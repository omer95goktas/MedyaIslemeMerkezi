
import csv
import pytesseract
from PIL import Image
from docx import Document
import traceback

import os; os.makedirs("/tmp/media_converter", exist_ok=True)
import os
os.makedirs("/tmp/media_converter", exist_ok=True)
from PIL import Image
import os
import sys
import re
import shutil
import subprocess
import asyncio
import tempfile
import uuid
import time
from typing import Optional, List, Dict, Any

from fastapi import FastAPI, UploadFile, File, Form, HTTPException, BackgroundTasks, Request, Query, Header, Depends
from fastapi.responses import FileResponse, JSONResponse, HTMLResponse, Response, StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
STATIC_DIR = os.path.join(BASE_DIR, "static")
TEMP_DIR = "/tmp/media_converter"
os.makedirs(STATIC_DIR, exist_ok=True)
os.makedirs(TEMP_DIR, exist_ok=True)

LIMIT_1GB = 1024 * 1024 * 1024
LIMIT_500MB = 500 * 1024 * 1024



app = FastAPI(title="Media & Document Converter API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

if os.path.exists(STATIC_DIR):
    app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")

async def save_with_limit(upload_file: UploadFile, dest_path: str, max_bytes: int):
    size = 0
    with open(dest_path, "wb") as f:
        while True:
            chunk = await upload_file.read(1024 * 1024)
            if not chunk:
                break
            size += len(chunk)
            if size > max_bytes:
                raise HTTPException(status_code=413, detail="Dosya boyutu cok buyuk.")
            f.write(chunk)

def cleanup_files(*files):
    for f in files:
        if f and os.path.exists(f):
            try:
                os.remove(f)
            except Exception as e:
                pass

def convert_any_to_epub(in_path, out_path, title="Belge"):
    t_file = out_path + "_temp.txt"
    try:
        text = ""
        if in_path.lower().endswith(".pdf"):
            try:
                import fitz
                doc = fitz.open(in_path)
                for page in doc:
                    text += page.get_text() + chr(10) + chr(10)
                doc.close()
            except Exception as e:
                from pypdf import PdfReader
                reader = PdfReader(in_path)
                for page in reader.pages:
                    t = page.extract_text()
                    if t:
                        text += t + chr(10) + chr(10)
        else:
            with open(in_path, "r", encoding="utf-8", errors="ignore") as f:
                text = f.read()

        if not text.strip():
            text = "Icerik okunamadi veya bos belge."

        with open(t_file, "w", encoding="utf-8") as tf:
            tf.write(text)

        safe_t = re.sub(r'[^a-zA-Z0-9_ ]', '', str(title)) or "E-Kitap"
        cmd = ["pandoc", t_file, "-o", out_path, "--metadata", "title=" + safe_t, "--metadata", "lang=tr"]
        subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=900)
    except Exception as e:
        print("EPUB hatasi:", str(e))
    finally:
        cleanup_files(t_file)

@app.get("/", response_class=HTMLResponse)
async def serve_home():
    index_path = os.path.join(STATIC_DIR, "index.html")
    if not os.path.exists(index_path):
        raise HTTPException(status_code=404, detail="Arayuz bulunamadi.")
    with open(index_path, "r", encoding="utf-8") as f:
        return f.read()

@app.get("/gizlilik-politikasi", response_class=HTMLResponse)
async def serve_privacy():
    privacy_path = os.path.join(STATIC_DIR, "privacy.html")
    if not os.path.exists(privacy_path):
        raise HTTPException(status_code=404, detail="Gizlilik politikasi sayfasi bulunamadi.")
    with open(privacy_path, "r", encoding="utf-8") as f:
        return f.read()

def build_audio_ffmpeg_cmd(in_path: str, out_path: str, fmt: str):
    cmd = ['ffmpeg', '-y', '-i', in_path, '-vn', '-sn']
    if fmt == 'mp3':
        cmd.extend(['-c:a', 'libmp3lame', '-b:a', '320k'])
    elif fmt == 'wav':
        cmd.extend(['-c:a', 'pcm_s16le'])
    elif fmt in ['m4a', 'aac']:
        cmd.extend(['-c:a', 'aac', '-b:a', '320k'])
    elif fmt == 'm4r':
        cmd.extend(['-c:a', 'aac', '-b:a', '256k', '-f', 'ipod'])
    elif fmt == 'flac':
        cmd.extend(['-c:a', 'flac', '-compression_level', '8'])
    elif fmt == 'alac':
        cmd.extend(['-c:a', 'alac', '-f', 'ipod'])
    elif fmt == 'opus':
        cmd.extend(['-c:a', 'libopus', '-b:a', '192k'])
    elif fmt == 'ogg':
        cmd.extend(['-c:a', 'libvorbis', '-q:a', '6'])
    elif fmt == 'wma':
        cmd.extend(['-c:a', 'wmav2', '-b:a', '256k'])
    elif fmt == 'aiff':
        cmd.extend(['-c:a', 'pcm_s16be'])
    elif fmt == 'ac3':
        cmd.extend(['-c:a', 'ac3', '-b:a', '384k'])
    elif fmt == 'mp2':
        cmd.extend(['-c:a', 'mp2', '-b:a', '192k'])
    elif fmt == 'amr':
        cmd.extend(['-c:a', 'libopencore_amrnb', '-ar', '8000', '-ac', '1'])
    elif fmt == '3ga':
        cmd.extend(['-c:a', 'libopencore_amrnb', '-ar', '8000', '-ac', '1', '-f', '3gp'])
    cmd.append(out_path)
    return cmd

@app.post("/api/video-to-audio")
async def video_to_audio(
    background_tasks: BackgroundTasks,
    video: UploadFile = File(...),
    format: str = Form("mp3")
):
    valid_formats = ["mp3", "m4a", "m4r", "wav", "flac", "alac", "aiff", "opus", "ogg", "aac", "ac3", "wma", "mp2"]
    if format not in valid_formats:
        return JSONResponse(status_code=400, content={"message": "Gecersiz ses formati secildi."})

    task_id = str(uuid.uuid4())
    in_ext = os.path.splitext(video.filename)[1] or ".mp4"
    in_path = os.path.join(TEMP_DIR, f"{task_id}_in{in_ext}")
    
    file_num = int(time.time())
    out_ext = "m4a" if format == "alac" else ("amr" if format in ["amr", "3ga"] else format)
    out_filename = f"omer_goktas_net_{file_num}.{out_ext}"
    out_path = os.path.join(TEMP_DIR, f"{task_id}_out.{out_ext}")

    try:
        await save_with_limit(video, in_path, LIMIT_1GB)
    except HTTPException as e:
        return JSONResponse(status_code=e.status_code, content={"message": e.detail})

    cmd = build_audio_ffmpeg_cmd(in_path, out_path, format)
    process = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=900)
    if process.returncode != 0:
        cleanup_files(in_path, out_path)
        err_msg = process.stderr.decode('utf-8', errors='ignore') if process.stderr else "Bilinmeyen hata"
        print(f"[FFMPEG V2A HATA]: {err_msg}")
        return JSONResponse(status_code=400, content={"message": "Videodan ses ayıklanamadı veya video dosyasında ses parçası bulunmuyor."})
    background_tasks.add_task(cleanup_files, in_path, out_path)
    return FileResponse(path=out_path, filename=out_filename, media_type="application/octet-stream", headers={"Access-Control-Expose-Headers": "Content-Disposition"})

@app.post("/api/audio-to-audio")
async def audio_to_audio(
    background_tasks: BackgroundTasks,
    audio: UploadFile = File(...),
    format: str = Form("mp3")
):
    valid_formats = ["mp3", "m4a", "m4r", "wav", "flac", "alac", "aiff", "opus", "ogg", "aac", "ac3", "wma", "mp2"]
    if format not in valid_formats:
        return JSONResponse(status_code=400, content={"message": "Gecersiz ses formati secildi."})

    task_id = str(uuid.uuid4())
    in_ext = os.path.splitext(audio.filename)[1] or ".mp3"
    in_path = os.path.join(TEMP_DIR, f"{task_id}_ain{in_ext}")
    
    file_num = int(time.time())
    out_ext = "m4a" if format == "alac" else ("amr" if format in ["amr", "3ga"] else format)
    out_filename = f"omer_goktas_net_{file_num}.{out_ext}"
    out_path = os.path.join(TEMP_DIR, f"{task_id}_aout.{out_ext}")

    try:
        await save_with_limit(audio, in_path, LIMIT_1GB)
    except HTTPException as e:
        return JSONResponse(status_code=e.status_code, content={"message": e.detail})

    cmd = build_audio_ffmpeg_cmd(in_path, out_path, format)
    process = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=900)
    if process.returncode != 0:
        cleanup_files(in_path, out_path)
        return JSONResponse(status_code=400, content={"message": "Ses formati donusturulemedi veya dosya bozuk."})

    
    background_tasks.add_task(cleanup_files, in_path, out_path)
    return FileResponse(path=out_path, filename=out_filename, media_type="application/octet-stream", headers={"Access-Control-Expose-Headers": "Content-Disposition"})

@app.post("/api/video-to-video")
async def video_to_video(
    background_tasks: BackgroundTasks,
    video: UploadFile = File(...),
    format: str = Form("mp4"),
    gif_start: float = Form(0.0),
    gif_duration: float = Form(5.0),
    gif_width: int = Form(480),
    gif_fps: int = Form(15)
):
    valid_formats = ["mp4", "m4v", "mov", "gif", "mkv", "avi", "webm", "3gp", "wmv", "flv", "ts", "vob", "ogv"]
    if format not in valid_formats:
        return JSONResponse(status_code=400, content={"message": "Gecersiz video formati secildi."})

    task_id = str(uuid.uuid4())
    in_ext = os.path.splitext(video.filename)[1] or ".mp4"
    in_path = os.path.join(TEMP_DIR, f"{task_id}_vin{in_ext}")
    
    file_num = int(time.time())
    out_filename = f"omer_goktas_net_{file_num}.{format}"
    out_path = os.path.join(TEMP_DIR, f"{task_id}_vout.{format}")

    try:
        await save_with_limit(video, in_path, LIMIT_1GB)
    except HTTPException as e:
        return JSONResponse(status_code=e.status_code, content={"message": e.detail})

    if format == "gif":
        duration = min(max(gif_duration, 0.5), 60.0)
        start_time = max(gif_start, 0.0)
        width = min(max(gif_width, 160), 1920)
        fps = min(max(gif_fps, 5), 30)
        # scale=w:-2 tek sayili piksel hatasini onler, lanczos ile yuksek kalite saglar
        vf_palette = f"fps={fps},scale={width}:-2:flags=lanczos,split[s0][s1];[s0]palettegen=max_colors=256:reserve_transparent=0[p];[s1][p]paletteuse=dither=bayer:bayer_scale=3"
        cmd = [
            "ffmpeg", "-y", "-threads", "0",
            "-ss", str(start_time),
            "-t", str(duration),
            "-i", in_path,
            "-vf", vf_palette,
            "-an",
            "-loop", "0",
            out_path
        ]
    elif format == "3gp":
        cmd = [
            "ffmpeg", "-y", "-threads", "0", "-i", in_path,
            "-r", "20", "-s", "352x288", "-c:v", "h263", "-b:v", "512k", "-c:a", "aac", "-b:a", "128k", "-ac", "1",
            "-metadata", "comment=medya_omergoktasnet", "-metadata", "title=medya_omergoktasnet",
            out_path
        ]
    elif format == "webm":
        cmd = [
            "ffmpeg", "-y", "-threads", "0", "-i", in_path,
            "-c:v", "libvpx-vp9", "-crf", "22", "-b:v", "0", "-deadline", "good", "-cpu-used", "4",
            "-c:a", "libopus", "-b:a", "192k",
            "-metadata", "comment=medya_omergoktasnet", "-metadata", "title=medya_omergoktasnet",
            out_path
        ]
    elif format == "ogv":
        cmd = [
            "ffmpeg", "-y", "-threads", "0", "-i", in_path,
            "-c:v", "libtheora", "-q:v", "7", "-c:a", "libvorbis", "-q:a", "6",
            "-metadata", "comment=medya_omergoktasnet", "-metadata", "title=medya_omergoktasnet",
            out_path
        ]
    elif format == "wmv":
        cmd = [
            "ffmpeg", "-y", "-threads", "0", "-i", in_path,
            "-c:v", "wmv2", "-q:v", "2", "-c:a", "wmav2", "-b:a", "256k",
            "-metadata", "comment=medya_omergoktasnet", "-metadata", "title=medya_omergoktasnet",
            out_path
        ]
    elif format == "flv":
        cmd = [
            "ffmpeg", "-y", "-threads", "0", "-i", in_path,
            "-c:v", "flv1", "-q:v", "2", "-c:a", "mp3", "-b:a", "256k",
            "-metadata", "comment=medya_omergoktasnet", "-metadata", "title=medya_omergoktasnet",
            out_path
        ]
    elif format == "vob":
        cmd = [
            "ffmpeg", "-y", "-threads", "0", "-i", in_path,
            "-target", "pal-dvd",
            out_path
        ]
    else:
        # H.264 Veryfast + CRF 18: Yüksek hız ve stüdyo netliği
        cmd = [
            "ffmpeg", "-y", "-threads", "0", "-i", in_path,
            "-c:v", "libx264", "-crf", "18", "-preset", "veryfast",
            "-c:a", "aac", "-b:a", "320k", "-pix_fmt", "yuv420p",
            "-metadata", "comment=medya_omergoktasnet", "-metadata", "title=medya_omergoktasnet",
            out_path
        ]

    process = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=900)
    if process.returncode != 0:
        cleanup_files(in_path, out_path)
        return JSONResponse(status_code=400, content={"message": "Video donusturulemedi, dosya bozuk veya desteklenmiyor olabilir."})

    
    background_tasks.add_task(cleanup_files, in_path, out_path)
    return FileResponse(path=out_path, filename=out_filename, media_type="application/octet-stream", headers={"Access-Control-Expose-Headers": "Content-Disposition"})

@app.post("/api/audio-to-video")
async def audio_to_video(
    background_tasks: BackgroundTasks,
    audio: UploadFile = File(...),
    image: UploadFile = File(...),
    resolution: str = Form("1080p_horizontal")
):
    task_id = str(uuid.uuid4())
    audio_ext = os.path.splitext(audio.filename)[1] or ".mp3"
    image_ext = os.path.splitext(image.filename)[1] or ".jpg"

    audio_path = os.path.join(TEMP_DIR, f"{task_id}_audio{audio_ext}")
    image_path = os.path.join(TEMP_DIR, f"{task_id}_img{image_ext}")
    
    file_num = int(time.time())
    out_filename = f"omer_goktas_net_{file_num}.mp4"
    out_path = os.path.join(TEMP_DIR, f"{task_id}_out.mp4")

    try:
        await save_with_limit(audio, audio_path, LIMIT_1GB)
        await save_with_limit(image, image_path, LIMIT_1GB)
    except HTTPException as e:
        cleanup_files(audio_path, image_path)
        return JSONResponse(status_code=e.status_code, content={"message": e.detail})

    res_map = {
        "4k_horizontal": (3840, 2160),
        "1440p_horizontal": (2560, 1440),
        "1080p_horizontal": (1920, 1080),
        "720p_horizontal": (1280, 720),
        "480p_horizontal": (854, 480),
        "4k_vertical": (2160, 3840),
        "1440p_vertical": (1440, 2560),
        "1080p_vertical": (1080, 1920),
        "720p_vertical": (720, 1280),
        "480p_vertical": (480, 854),
        "1080p_square": (1080, 1080)
    }

    width, height = res_map.get(resolution, (1920, 1080))
    # Resmi kirpmadan tam ekrana sigdir ve uymayan kisimlari siyah (pad) yap
    vf_filter = f"scale={width}:{height}:force_original_aspect_ratio=decrease,pad={width}:{height}:(ow-iw)/2:(oh-ih)/2,setsar=1,format=yuv420p"

    # preset ultrafast + tune stillimage: Statik görsel render hızını 10 katına çıkarır
    cmd = [
        "ffmpeg", "-y", "-threads", "0",
        "-loop", "1", "-framerate", "2", "-i", image_path,
        "-i", audio_path,
        "-vf", vf_filter,
        "-c:v", "libx264", "-crf", "16", "-tune", "stillimage", "-preset", "ultrafast",
        "-c:a", "aac", "-b:a", "320k",
        "-pix_fmt", "yuv420p",
        "-shortest",
        "-metadata", "comment=medya_omergoktasnet",
        "-metadata", "encoded_by=medya_omergoktasnet",
        "-metadata", "title=medya_omergoktasnet",
        out_path
    ]

    process = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=900)
    if process.returncode != 0:
        cleanup_files(audio_path, image_path, out_path)
        return JSONResponse(status_code=400, content={"message": "Gorsel veya ses dosyasi okunamadi."})

    background_tasks.add_task(cleanup_files, audio_path, image_path, out_path)
    return FileResponse(path=out_path, filename=out_filename, media_type="video/mp4", headers={"Access-Control-Expose-Headers": "Content-Disposition"})

@app.post("/api/image-to-image")
async def image_to_image(
    background_tasks: BackgroundTasks,
    image: UploadFile = File(...),
    format: str = Form("jpg")
):
    valid_formats = ["jpg", "jpeg", "png", "webp", "ico", "pdf", "bmp", "tiff", "gif", "avif", "tga"]
    if format not in valid_formats:
        return JSONResponse(status_code=400, content={"message": "Gecersiz resim formati secildi."})

    task_id = str(uuid.uuid4())
    in_ext = os.path.splitext(image.filename)[1] or ".jpg"
    in_path = os.path.join(TEMP_DIR, f"{task_id}_img_in{in_ext}")
    
    file_num = int(time.time())
    out_filename = f"omer_goktas_net_{file_num}.{format}"
    out_path = os.path.join(TEMP_DIR, f"{task_id}_img_out.{format}")

    try:
        await save_with_limit(image, in_path, LIMIT_1GB)
    except HTTPException as e:
        return JSONResponse(status_code=e.status_code, content={"message": e.detail})

    if format == "pdf":
        try:
            with Image.open(in_path) as pil_img:
                rgb_img = pil_img.convert("RGB")
                rgb_img.save(out_path, format="PDF", resolution=150.0, quality=95)
        except Exception as e:
            temp_png = os.path.join(TEMP_DIR, f"{task_id}_mid.png")
            subprocess.run(["ffmpeg", "-y", "-threads", "0", "-i", in_path, temp_png], stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=900)
            with Image.open(temp_png) as pil_img:
                rgb_img = pil_img.convert("RGB")
                rgb_img.save(out_path, format="PDF", resolution=150.0, quality=95)
            cleanup_files(temp_png)
    else:
        try:
            with Image.open(in_path) as pil_img:
                if format in ["jpg", "jpeg"]:
                    rgb_img = pil_img.convert("RGB")
                    rgb_img.save(out_path, format="JPEG", quality=95, optimize=False)
                elif format == "png":
                    pil_img.save(out_path, format="PNG", compress_level=1)
                elif format == "webp":
                    pil_img.save(out_path, format="WEBP", quality=95, method=2)
                elif format == "ico":
                    pil_img.save(out_path, format="ICO", sizes=[(256, 256)])
                elif format == "bmp":
                    pil_img.save(out_path, format="BMP")
                elif format == "tiff":
                    pil_img.save(out_path, format="TIFF", compression="lzw")
                else:
                    raise Exception("FFmpeg fallback")
        except Exception as e:
            cmd = ["ffmpeg", "-y", "-threads", "0", "-i", in_path]
            if format == "ico":
                cmd.extend(["-vf", "scale=256:256:force_original_aspect_ratio=increase,pad=256:256:(ow-iw)/2:(oh-ih)/2:color=black@0"])
            elif format in ["jpg", "jpeg"]:
                cmd.extend(["-q:v", "2"])
            elif format == "webp":
                cmd.extend(["-quality", "95"])
            elif format == "avif":
                cmd.extend(["-crf", "15", "-cpu-used", "6"])
            cmd.append(out_path)

            process = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=900)
            if process.returncode != 0:
                cleanup_files(in_path, out_path)
                return JSONResponse(status_code=400, content={"message": "Resim donusturulemedi."})

    
    background_tasks.add_task(cleanup_files, in_path, out_path)
    return FileResponse(path=out_path, filename=out_filename, media_type="application/pdf" if format == "pdf" else "application/octet-stream", headers={"Access-Control-Expose-Headers": "Content-Disposition"})

@app.post("/api/merge-videos")
async def merge_videos(
    background_tasks: BackgroundTasks,
    video1: UploadFile = File(...),
    video2: UploadFile = File(...),
    resolution: str = Form("1080p")
):
    task_id = str(uuid.uuid4())
    in_ext1 = os.path.splitext(video1.filename)[1] or ".mp4"
    in_ext2 = os.path.splitext(video2.filename)[1] or ".mp4"

    in_path1 = os.path.join(TEMP_DIR, f"{task_id}_v1{in_ext1}")
    in_path2 = os.path.join(TEMP_DIR, f"{task_id}_v2{in_ext2}")
    
    file_num = int(time.time())
    out_filename = f"omer_goktas_net_{file_num}.mp4"
    out_path = os.path.join(TEMP_DIR, f"{task_id}_merged.mp4")

    try:
        await save_with_limit(video1, in_path1, LIMIT_1GB)
        await save_with_limit(video2, in_path2, LIMIT_1GB)
    except HTTPException as e:
        cleanup_files(in_path1, in_path2)
        return JSONResponse(status_code=e.status_code, content={"message": e.detail})

    res_map = {
        "480p": "854:480",
        "720p": "1280:720",
        "1080p": "1920:1080",
        "2k": "2560:1440",
        "4k": "3840:2160"
    }
    scale_target = res_map.get(resolution.lower(), "1920:1080")

    filter_complex = (
        f"[0:v]scale={scale_target}:force_original_aspect_ratio=decrease,pad={scale_target}:(ow-iw)/2:(oh-ih)/2,setsar=1,fps=30[v0];"
        f"[0:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo[a0];"
        f"[1:v]scale={scale_target}:force_original_aspect_ratio=decrease,pad={scale_target}:(ow-iw)/2:(oh-ih)/2,setsar=1,fps=30[v1];"
        f"[1:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo[a1];"
        f"[v0][a0][v1][a1]concat=n=2:v=1:a=1[v][a]"
    )

    cmd = [
        "ffmpeg", "-y", "-threads", "0",
        "-i", in_path1,
        "-i", in_path2,
        "-filter_complex", filter_complex,
        "-map", "[v]",
        "-map", "[a]",
        "-c:v", "libx264", "-preset", "ultrafast", "-crf", "23",
        "-c:a", "aac", "-b:a", "192k",
        "-pix_fmt", "yuv420p",
        out_path
    ]

    process = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=1200)

    if process.returncode != 0:
        print("[VIDEO BIRLESTIRME HATASI]:", process.stderr.decode("utf-8", errors="ignore"))
        cleanup_files(in_path1, in_path2, out_path)
        return JSONResponse(status_code=400, content={"message": "Videolar birleştirilemedi. Formatlar uyuşmuyor veya dosya bozuk."})

    background_tasks.add_task(cleanup_files, in_path1, in_path2, out_path)
    return FileResponse(path=out_path, filename=out_filename, media_type="video/mp4", headers={"Access-Control-Expose-Headers": "Content-Disposition"})



@app.post("/api/merge-audios")
async def merge_audios(
    background_tasks: BackgroundTasks,
    audio1: UploadFile = File(...),
    audio2: UploadFile = File(...),
    format: str = Form("mp3")
):
    task_id = str(uuid.uuid4())
    in_ext1 = os.path.splitext(audio1.filename)[1] or ".mp3"
    in_ext2 = os.path.splitext(audio2.filename)[1] or ".mp3"
    in_path1 = os.path.join(TEMP_DIR, f"{task_id}_in1{in_ext1}")
    in_path2 = os.path.join(TEMP_DIR, f"{task_id}_in2{in_ext2}")
    
    format_clean = format.lower().strip()
    out_filename = f"omer_goktas_net_{int(time.time()*1000)}.{format_clean}"
    out_path = os.path.join(TEMP_DIR, f"{task_id}_merged.{format_clean}")

    try:
        await save_with_limit(audio1, in_path1, LIMIT_1GB)
        await save_with_limit(audio2, in_path2, LIMIT_1GB)
        s1 = os.path.getsize(in_path1) if os.path.exists(in_path1) else 0
        s2 = os.path.getsize(in_path2) if os.path.exists(in_path2) else 0
        if s1 + s2 > LIMIT_1GB:
            cleanup_files(in_path1, in_path2)
            return JSONResponse(status_code=413, content={"message": "İki sesin toplam boyutu 1 GB sınırını aşıyor."})
    except HTTPException as e:
        cleanup_files(in_path1, in_path2)
        return JSONResponse(status_code=e.status_code, content={"message": e.detail})
    except Exception as e:
        cleanup_files(in_path1, in_path2)
        return JSONResponse(status_code=500, content={"message": f"Dosya kaydetme hatası: {str(e)}"})

    filter_complex = (
        "[0:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo[a0];"
        "[1:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo[a1];"
        "[a0][a1]concat=n=2:v=0:a=1[aout]"
    )

    codec_args = []
    if format_clean == "mp3":
        codec_args = ["-c:a", "libmp3lame", "-q:a", "2"]
    elif format_clean in ["m4a", "aac"]:
        codec_args = ["-c:a", "aac", "-b:a", "320k"]
    elif format_clean == "wav":
        codec_args = ["-c:a", "pcm_s16le"]
    elif format_clean == "flac":
        codec_args = ["-c:a", "flac"]
    elif format_clean == "ogg":
        codec_args = ["-c:a", "libvorbis", "-q:a", "6"]
    else:
        codec_args = ["-c:a", "libmp3lame", "-q:a", "2"]

    cmd = [
        "ffmpeg", "-y", "-threads", "0",
        "-i", in_path1,
        "-i", in_path2,
        "-filter_complex", filter_complex,
        "-map", "[aout]"
    ] + codec_args + [
        "-metadata", "comment=medya_omergoktasnet",
        "-metadata", "encoded_by=medya_omergoktasnet",
        "-metadata", "title=medya_omergoktasnet",
        out_path
    ]

    process = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=600)
    if process.returncode != 0:
        print("[SES BIRLESTIRME FFMPEG HATASI]:", process.stderr.decode("utf-8", errors="ignore"))
        cleanup_files(in_path1, in_path2, out_path)
        return JSONResponse(status_code=400, content={"message": "Ses dosyaları birleştirilemedi."})

    media_type_map = {
        "mp3": "audio/mpeg",
        "wav": "audio/wav",
        "m4a": "audio/mp4",
        "aac": "audio/aac",
        "flac": "audio/flac",
        "ogg": "audio/ogg"
    }
    m_type = media_type_map.get(format_clean, "application/octet-stream")

    background_tasks.add_task(cleanup_files, in_path1, in_path2, out_path)
    return FileResponse(path=out_path, filename=out_filename, media_type=m_type, headers={"Access-Control-Expose-Headers": "Content-Disposition"})

# --- PDF İŞLEMLERİ (BÖLME VE BİRLEŞTİRME) ---
import os
import shutil
import uuid
import time
import zipfile
import tempfile
import asyncio
from pypdf import PdfReader, PdfWriter
from fastapi import UploadFile, File, BackgroundTasks, HTTPException
from fastapi.responses import FileResponse

async def safe_delete_delayed(file_path: str, delay_seconds: int = 20):
    await asyncio.sleep(delay_seconds)
    if os.path.exists(file_path):
        try:
            os.remove(file_path)
        except Exception:
            pass

@app.post("/api/split-pdf")
async def split_pdf(
    background_tasks: BackgroundTasks,
    pdf_file: UploadFile = File(...)
):
    temp_dir = tempfile.gettempdir()
    ts = int(time.time() * 1000)
    prefix_name = f"omer_goktas_net_{ts}"
    unique_id = uuid.uuid4().hex[:6]
    
    in_path = os.path.join(temp_dir, f"split_in_{unique_id}.pdf")
    out_zip_name = f"{prefix_name}_sayfalar.zip"
    out_zip_path = os.path.join(temp_dir, f"split_out_{unique_id}.zip")

    try:
        with open(in_path, "wb") as f_in:
            shutil.copyfileobj(pdf_file.file, f_in)

        reader = PdfReader(in_path)
        total_pages = len(reader.pages)
        if total_pages == 0:
            raise HTTPException(status_code=400, detail="PDF belgesi bos.")

        with zipfile.ZipFile(out_zip_path, "w", zipfile.ZIP_DEFLATED) as zipf:
            for idx, page in enumerate(reader.pages):
                writer = PdfWriter()
                writer.add_page(page)
                page_path = os.path.join(temp_dir, f"temp_p_{unique_id}_{idx}.pdf")
                with open(page_path, "wb") as pf:
                    writer.write(pf)
                zipf.write(page_path, arcname=f"{prefix_name}_sayfa_{idx + 1}.pdf")
                if os.path.exists(page_path):
                    try:
                        os.remove(page_path)
                    except Exception:
                        pass

        background_tasks.add_task(safe_delete_delayed, out_zip_path, 25)
        return FileResponse(
            path=out_zip_path,
            filename=out_zip_name,
            media_type="application/zip",
            headers={
                "Access-Control-Expose-Headers": "Content-Disposition",
                "Content-Disposition": f'attachment; filename="{out_zip_name}"'
            }
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"PDF bolme hatasi: {str(e)}")
    finally:
        if os.path.exists(in_path):
            try:
                os.remove(in_path)
            except Exception:
                pass

@app.post("/api/merge-pdfs")
@app.post("/api/merge-pdf")
async def merge_pdf(
    background_tasks: BackgroundTasks,
    pdf1: UploadFile = File(...),
    pdf2: UploadFile = File(None)
):
    temp_dir = tempfile.gettempdir()
    ts = int(time.time() * 1000)
    out_pdf_name = f"omer_goktas_net_{ts}.pdf"
    out_pdf_path = os.path.join(temp_dir, f"merge_out_{uuid.uuid4().hex[:6]}.pdf")
    temp_inputs = []
    
    try:
        writer = PdfWriter()
        files = [pdf1]
        if pdf2 is not None and pdf2.filename:
            files.append(pdf2)

        for f_upload in files:
            t_path = os.path.join(temp_dir, f"merge_in_{uuid.uuid4().hex[:8]}.pdf")
            temp_inputs.append(t_path)
            with open(t_path, "wb") as bf:
                shutil.copyfileobj(f_upload.file, bf)
            
            reader = PdfReader(t_path)
            for page in reader.pages:
                writer.add_page(page)

        with open(out_pdf_path, "wb") as f_out:
            writer.write(f_out)

        background_tasks.add_task(safe_delete_delayed, out_pdf_path, 25)
        return FileResponse(
            path=out_pdf_path,
            filename=out_pdf_name,
            media_type="application/pdf",
            headers={
                "Access-Control-Expose-Headers": "Content-Disposition",
                "Content-Disposition": f'attachment; filename="{out_pdf_name}"'
            }
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"PDF birlestirme hatasi: {str(e)}")
    finally:
        for tf in temp_inputs:
            if os.path.exists(tf):
                try:
                    os.remove(tf)
                except Exception:
                    pass








# --- HIZLI & AKILLI PDF OCR (GELISTIRILMIS KALITE) ---
import os
import shutil
import uuid
import time
import tempfile
import asyncio
import numpy as np
import cv2
from PIL import Image
from fastapi import UploadFile, File, BackgroundTasks, HTTPException
from fastapi.responses import FileResponse
from pypdf import PdfReader
from pdf2image import convert_from_path
import pytesseract

def preprocess_image_for_ocr(pil_img):
    """Gorseli gri tona cevirip kontrast/gurultu temizligi yaparak OCR kalitesini artirir."""
    try:
        # PIL to OpenCV
        cv_img = np.array(pil_img)
        if len(cv_img.shape) == 3:
            gray = cv2.cvtColor(cv_img, cv2.COLOR_RGB2GRAY)
        else:
            gray = cv_img

        # Gurultu azaltma ve adaptif esikleme (Netlestirme)
        # Metinleri koyu siyah, arkaplani saf beyaz yapar
        gray = cv2.medianBlur(gray, 1)
        thresh = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)[1]
        
        return Image.fromarray(thresh)
    except Exception:
        return pil_img

@app.post("/api/pdf-ocr")
async def pdf_ocr_endpoint(
    background_tasks: BackgroundTasks,
    pdf_file: UploadFile = File(...)
):
    temp_dir = tempfile.gettempdir()
    ts = int(time.time() * 1000)
    unique_id = uuid.uuid4().hex[:6]
    
    in_path = os.path.join(temp_dir, f"ocr_in_{unique_id}.pdf")
    out_txt_name = f"omer_goktas_net_{ts}_metin.txt"
    out_txt_path = os.path.join(temp_dir, out_txt_name)

    try:
        with open(in_path, "wb") as f_in:
            shutil.copyfileobj(pdf_file.file, f_in)

        # 1. ADIM: Dijital Metin Katmani Kontrolu
        reader = PdfReader(in_path)
        total_pages = len(reader.pages)
        if total_pages == 0:
            raise HTTPException(status_code=400, detail="PDF belgesi bos.")

        digital_text = []
        has_embedded_text = False
        valid_page_count = 0
        
        for idx, page in enumerate(reader.pages):
            p_text = page.extract_text() or ""
            if len(p_text.strip()) > 30:
                valid_page_count += 1
            digital_text.append(f"--- SAYFA {idx + 1} ---\n\n{p_text.strip()}\n\n")

        # Eger sayfalarin cogu dijital metin barindiriyorsa dogrudan cikar
        if valid_page_count > (total_pages * 0.4):
            full_text = "".join(digital_text)
        else:
            # 2. ADIM: Gelismis Gorsel Isleme + Tesseract OCR
            ocr_text = []
            tess_custom_config = r'--oem 3 --psm 3 -l tur+eng'
            
            for p_num in range(1, total_pages + 1):
                page_images = convert_from_path(
                    in_path,
                    dpi=200,
                    first_page=p_num,
                    last_page=p_num,
                    thread_count=2
                )
                if page_images:
                    raw_img = page_images[0]
                    # On Isleme (Netlestirme ve kontrast)
                    clean_img = preprocess_image_for_ocr(raw_img)
                    
                    text = pytesseract.image_to_string(clean_img, config=tess_custom_config)
                    ocr_text.append(f"--- SAYFA {p_num} ---\n\n{text.strip()}\n\n")
                    
                    del page_images
                    del raw_img
                    del clean_img

            full_text = "".join(ocr_text)

        if not full_text.strip():
            full_text = "Belgeden metin okunamadi veya belge bos."

        with open(out_txt_path, "w", encoding="utf-8") as f_out:
            f_out.write(full_text)

        background_tasks.add_task(safe_delete_delayed, out_txt_path, 30)
        return FileResponse(
            path=out_txt_path,
            filename=out_txt_name,
            media_type="text/plain; charset=utf-8",
            headers={
                "Access-Control-Expose-Headers": "Content-Disposition",
                "Content-Disposition": f'attachment; filename="{out_txt_name}"'
            }
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Metin cikarma hatasi: {str(e)}")
    finally:
        if os.path.exists(in_path):
            try:
                os.remove(in_path)
            except Exception:
                pass


# ============================================================
# GERI BILDIRIM (FEEDBACK) ENDPOINT - Telegram Bildirimi
# ============================================================
import aiohttp
from datetime import datetime

TELEGRAM_BOT_TOKEN = "7711639636:AAHzN0gFLP-eBgsV786uJcTWklZ47VnLNTc"
TELEGRAM_CHAT_ID = "6087699112"

async def send_telegram_message(text: str):
    url = f"https://api.telegram.org/bot{TELEGRAM_BOT_TOKEN}/sendMessage"
    payload = {
        "chat_id": TELEGRAM_CHAT_ID,
        "text": text,
        "parse_mode": "HTML"
    }
    try:
        async with aiohttp.ClientSession() as session:
            async with session.post(url, json=payload, timeout=aiohttp.ClientTimeout(total=10)) as resp:
                return await resp.json()
    except Exception as e:
        print(f"Telegram gonderim hatasi: {e}")

import time
from fastapi import Request

import time
import json
from fastapi import Request

FEEDBACK_RATE_LIMIT = {} # ip -> timestamp
FEEDBACK_LOG_PATH = "/root/media-converter/feedback_logs.json"

@app.post("/api/feedback")
async def submit_feedback(
    request: Request,
    tool_name: str = Form(...),
    error_type: str = Form(...),
    source_format: str = Form(...),
    target_format: str = Form(...),
    description: str = Form(...),
    website_url: str = Form(""),
    android_version: str = Form(""),
    app_version: str = Form("")
):
    # 1. Honeypot check for bots
    if website_url and website_url.strip():
        return JSONResponse(content={"success": True, "message": "Geri bildiriminiz iletildi."})

    # 2. IP extraction & Rate limiting (1 request per 10 minutes)
    forwarded = request.headers.get("x-forwarded-for", "")
    if forwarded:
        client_ip = forwarded.split(",")[0].strip()
    else:
        client_ip = request.client.host if request.client else "unknown"

    now_ts = time.time()
    last_req = FEEDBACK_RATE_LIMIT.get(client_ip, 0)
    if (now_ts - last_req) < 600:
        remaining = int(600 - (now_ts - last_req))
        raise HTTPException(
            status_code=429,
            detail=f"Çok fazla bildirim gönderdiniz. Lütfen {remaining // 60 + 1} dakika sonra tekrar deneyin."
        )

    # 3. Input Validation
    clean_desc = description.strip()
    if len(clean_desc) < 15:
        raise HTTPException(
            status_code=422,
            detail="Açıklama alanı çok kısa. Lütfen en az 15 karakter ile sorunu açıklayın."
        )

    clean_source = source_format.strip().upper().replace(".", "")
    clean_target = target_format.strip().upper().replace(".", "")

    if not clean_source or not clean_target or not tool_name.strip():
        raise HTTPException(
            status_code=422,
            detail="Lütfen kaynak format, hedef format ve araç bilgilerini eksiksiz seçin."
        )

    # 4. Update Rate Limit
    FEEDBACK_RATE_LIMIT[client_ip] = now_ts

    # 5. Structured JSON Log
    now_str = datetime.now().strftime("%d.%m.%Y %H:%M:%S")
    log_entry = {
        "timestamp": now_str,
        "ip": client_ip,
        "tool_name": tool_name.strip(),
        "error_type": error_type.strip(),
        "source_format": clean_source,
        "target_format": clean_target,
        "description": clean_desc,
        "android_version": android_version.strip() if android_version else "Web",
        "app_version": app_version.strip() if app_version else "Web",
        "user_agent": request.headers.get("user-agent", "")
    }

    try:
        with open(FEEDBACK_LOG_PATH, "a", encoding="utf-8") as f_log:
            f_log.write(json.dumps(log_entry, ensure_ascii=False) + chr(10))
    except Exception as e:
        print(f"Feedback log kayit hatasi: {e}")

    # 6. Telegram notification
    client_label = "📱 Android Uygulaması" if android_version else "🌐 Web Sitesi"
    lines = [
        "🚨 <b>Yeni Hata Bildirimi (Doğrulanmış)</b>",
        f"🛠️ <b>Araç:</b> {tool_name.strip()}",
        f"⚠️ <b>Hata Türü:</b> {error_type.strip()}",
        f"📥 <b>Kaynak Format:</b> {clean_source}",
        f"📤 <b>Hedef Format:</b> {clean_target}",
        f"📝 <b>Açıklama:</b> {clean_desc}",
        f"🖥️ <b>Platform:</b> {client_label} ({app_version if app_version else 'v1.0'})",
        f"📍 <b>IP:</b> {client_ip}",
        f"⏰ <b>Zaman:</b> {now_str}"
    ]
    message = chr(10).join(lines)

    try:
        await send_telegram_message(message)
    except Exception as e:
        print(f"Telegram bildirim hatasi: {e}")

    return JSONResponse(content={"success": True, "message": "Geri bildiriminiz başarıyla iletildi."})



@app.post("/api/epub-to-pdf")
async def epub_to_pdf(
    background_tasks: BackgroundTasks,
    file: UploadFile = File(...)
):
    task_id = str(uuid.uuid4())
    in_ext = os.path.splitext(file.filename)[1].lower() or ".epub"
    in_path = os.path.join(TEMP_DIR, f"{task_id}{in_ext}")
    
    file_num = int(time.time())
    out_filename = f"omer_goktas_net_{file_num}.pdf"
    out_path = os.path.join(TEMP_DIR, f"{task_id}.pdf")

    try:
        await save_with_limit(file, in_path, LIMIT_1GB)
    except HTTPException as e:
        cleanup_files(in_path)
        return JSONResponse(status_code=e.status_code, content={"message": e.detail})

    env = os.environ.copy()
    env["QT_QPA_PLATFORM"] = "offscreen"
    env["XDG_RUNTIME_DIR"] = "/tmp"
    env["HOME"] = "/tmp"
    env["CALIBRE_CONFIG_DIRECTORY"] = "/tmp/.config/calibre"
    env["QTWEBENGINE_CHROMIUM_FLAGS"] = "--no-sandbox --disable-setuid-sandbox --disable-gpu --disable-software-rasterizer"
    env["QT_QPA_PLATFORM"] = "offscreen"
    env["QTWEBENGINE_DISABLE_SANDBOX"] = "1"
    env["QT_QPA_PLATFORM"] = "offscreen"
    env["XDG_RUNTIME_DIR"] = "/tmp"
    env["QTWEBENGINE_CHROMIUM_FLAGS"] = "--no-sandbox --disable-setuid-sandbox --disable-gpu --disable-software-rasterizer"

    cmd = ["ebook-convert", in_path, out_path]
    process = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=env, timeout=600)

    if process.returncode != 0:
        print("[EPUB TO PDF HATASI]:", process.stderr.decode("utf-8", errors="ignore"))
        cleanup_files(in_path, out_path)
        return JSONResponse(status_code=400, content={"message": "EPUB dosyası PDF formatına dönüştürülemedi. Dosyanın geçerli olduğundan emin olun."})

    
    background_tasks.add_task(cleanup_files, in_path, out_path)
    return FileResponse(
        path=out_path,
        filename=out_filename,
        media_type="application/pdf",
        headers={"Access-Control-Expose-Headers": "Content-Disposition"}
    )

@app.post("/api/convert-document")
async def convert_document(
    background_tasks: BackgroundTasks,
    file: UploadFile = File(...),
    format: str = Form(...)
):
    task_id = str(uuid.uuid4())
    in_ext = os.path.splitext(file.filename)[1].lower()
    target_ext = format.lower().strip().replace(".", "")
    
    if not in_ext:
        in_ext = ".txt"
    src_ext = in_ext.replace(".", "")
    
    in_path = os.path.join(TEMP_DIR, f"{task_id}{in_ext}")
    file_num = int(time.time())
    out_filename = f"omer_goktas_net_{file_num}.{target_ext}"
    out_path = os.path.join(TEMP_DIR, f"{task_id}.{target_ext}")

    try:
        await save_with_limit(file, in_path, LIMIT_1GB)
    except HTTPException as e:
        cleanup_files(in_path)
        return JSONResponse(status_code=e.status_code, content={"message": e.detail})

    env = os.environ.copy()
    env["QT_QPA_PLATFORM"] = "offscreen"
    env["XDG_RUNTIME_DIR"] = "/tmp"
    env["HOME"] = "/tmp"
    env["CALIBRE_CONFIG_DIRECTORY"] = "/tmp/.config/calibre"
    env["QTWEBENGINE_CHROMIUM_FLAGS"] = "--no-sandbox --disable-setuid-sandbox --disable-gpu --disable-software-rasterizer"
    env["QT_QPA_PLATFORM"] = "offscreen"
    env["QTWEBENGINE_DISABLE_SANDBOX"] = "1"
    env["QT_QPA_PLATFORM"] = "offscreen"
    env["XDG_RUNTIME_DIR"] = "/tmp"
    env["QTWEBENGINE_CHROMIUM_FLAGS"] = "--no-sandbox --disable-setuid-sandbox --disable-gpu --disable-software-rasterizer"

    success = False

    # 1. HEDEF CSV ISE
    if target_ext == "csv" and src_ext != "csv":
        res = subprocess.run(["libreoffice", "--headless", "--convert-to", "csv", in_path, "--outdir", TEMP_DIR], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        lo_out = os.path.join(TEMP_DIR, f"{task_id}.csv")
        if os.path.exists(lo_out) and os.path.getsize(lo_out) > 0:
            if lo_out != out_path:
                os.replace(lo_out, out_path)
            success = True
        
        if not success:
            txt_temp = os.path.join(TEMP_DIR, f"{task_id}_temp.txt")
            if src_ext == "pdf":
                subprocess.run(["pdftotext", in_path, txt_temp], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            else:
                subprocess.run(["pandoc", in_path, "-o", txt_temp], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            
            if os.path.exists(txt_temp):
                try:
                    with open(txt_temp, "r", encoding="utf-8", errors="ignore") as tf, open(out_path, "w", encoding="utf-8", newline="") as cf:
                        writer = csv.writer(cf)
                        for line in tf:
                            cleaned = line.strip()
                            if cleaned:
                                writer.writerow([cleaned])
                    if os.path.exists(out_path) and os.path.getsize(out_path) > 0:
                        success = True
                except Exception:
                    pass
                cleanup_files(txt_temp)

    # 2. PDF KAYNAKLI DÖNÜŞÜMLER
    if not success and src_ext == "pdf":
        if target_ext in ["txt", "csv"]:
            res = subprocess.run(["pdftotext", in_path, out_path], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            if res.returncode == 0 and os.path.exists(out_path):
                success = True
        elif target_ext == "docx":
            try:
                from pdf2docx import Converter
                cv = Converter(in_path)
                cv.convert(out_path)
                cv.close()
                if os.path.exists(out_path):
                    success = True
            except Exception:
                pass
        elif target_ext in ["md", "odt", "html", "rtf", "epub"]:
            # PDF -> TXT -> Pandoc ile Hedefe
            txt_temp = os.path.join(TEMP_DIR, f"{task_id}_temp.txt")
            subprocess.run(["pdftotext", in_path, txt_temp], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            if os.path.exists(txt_temp):
                res = subprocess.run(["pandoc", txt_temp, "-o", out_path], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                if res.returncode == 0 and os.path.exists(out_path):
                    success = True
                cleanup_files(txt_temp)

        if not success:
            res = subprocess.run(["ebook-convert", in_path, out_path], stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=env, timeout=600)
            if res.returncode == 0 and os.path.exists(out_path):
                success = True

    # 3. GENEL DÖNÜŞÜM HATTI (Pandoc -> LibreOffice -> Calibre)
    if not success:
        res = subprocess.run(["pandoc", in_path, "-o", out_path], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        if res.returncode == 0 and os.path.exists(out_path):
            success = True

    if not success:
        res = subprocess.run(["libreoffice", "--headless", "--convert-to", target_ext, in_path, "--outdir", TEMP_DIR], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        lo_out = os.path.join(TEMP_DIR, f"{task_id}.{target_ext}")
        if os.path.exists(lo_out):
            if lo_out != out_path:
                os.replace(lo_out, out_path)
            success = True

    if not success:
        res = subprocess.run(["ebook-convert", in_path, out_path], stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=env, timeout=600)
        if res.returncode == 0 and os.path.exists(out_path):
            success = True

    if not success:
        cleanup_files(in_path, out_path)
        return JSONResponse(status_code=400, content={"message": f"{src_ext.upper()} dosyasını {target_ext.upper()} formatına dönüştürme işlemi başarısız oldu."})

    
    background_tasks.add_task(cleanup_files, in_path, out_path)
    return FileResponse(
        path=out_path,
        filename=out_filename,
        media_type="application/octet-stream",
        headers={"Access-Control-Expose-Headers": "Content-Disposition"}
    )




@app.post("/api/image-to-text")
async def image_to_text(
    background_tasks: BackgroundTasks,
    image: UploadFile = File(...),
    lang: str = Form("tur+eng")
):
    task_id = str(uuid.uuid4())
    in_ext = os.path.splitext(image.filename)[1].lower()
    if not in_ext:
        in_ext = ".png"
        
    in_path = os.path.join(TEMP_DIR, f"{task_id}{in_ext}")
    out_path = os.path.join(TEMP_DIR, f"{task_id}.txt")
    file_num = int(time.time())
    out_filename = f"omer_goktas_net_{file_num}.txt"

    try:
        await save_with_limit(image, in_path, LIMIT_1GB)
    except HTTPException as e:
        cleanup_files(in_path)
        return JSONResponse(status_code=e.status_code, content={"message": e.detail})

    try:
        img = Image.open(in_path)
        
        tess_lang = "tur" if "tur" in lang.lower() else "eng"
        if "tur" in lang.lower() and "eng" in lang.lower():
            tess_lang = "tur+eng"

        text = pytesseract.image_to_string(img, lang=tess_lang)
        text = text.strip()

        if not text:
            text = "Görselde okunabilir bir metin tespit edilemedi."
        with open(out_path, "w", encoding="utf-8") as f:
            f.write(text)
        background_tasks.add_task(cleanup_files, in_path, out_path)
        return FileResponse(
            path=out_path,
            filename=out_filename,
            media_type="text/plain; charset=utf-8",
            headers={"Access-Control-Expose-Headers": "Content-Disposition"}
        )

    except Exception as e:
        cleanup_files(in_path, out_path)
        return JSONResponse(status_code=500, content={"message": f"Görsel OCR hatası: {str(e)}"})


@app.post("/api/create-document")
async def create_document_endpoint(
    background_tasks: BackgroundTasks,
    doc_name: str = Form("belge"),
    format: str = Form("pdf"),
    content_html: str = Form("")
):
    format = format.lower().strip()
    clean_name = re.sub(r'[^a-zA-Z0-9_-]', '_', doc_name.strip()) or "belge"
    task_id = str(uuid.uuid4())
    in_html = os.path.join(TEMP_DIR, f"{task_id}_in.html")
    out_file = os.path.join(TEMP_DIR, f"{clean_name}_{task_id[:8]}.{format}")
    
    full_html = "<!DOCTYPE html><html><head><meta charset='utf-8'><title>" + clean_name + "</title>"
    full_html += "<style>body { font-family: Arial, sans-serif; margin: 20mm; line-height: 1.6; } h1, h2, h3 { color: #333; }</style>"
    full_html += "</head><body>" + content_html + "</body></html>"

    with open(in_html, "w", encoding="utf-8") as f:
        f.write(full_html)

    success = False
    try:
        if format == "html":
            shutil.copy(in_html, out_file)
            success = True
        elif format == "txt":
            subprocess.run(["pandoc", in_html, "-o", out_file], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            if not os.path.exists(out_file) or os.path.getsize(out_file) == 0:
                with open(out_file, "w", encoding="utf-8") as tf:
                    tf.write(re.sub(r'<[^>]+>', '', content_html))
            success = os.path.exists(out_file) and os.path.getsize(out_file) > 0
        elif format == "md":
            subprocess.run(["pandoc", in_html, "-o", out_file], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            success = os.path.exists(out_file) and os.path.getsize(out_file) > 0
        elif format == "docx":
            subprocess.run(["pandoc", in_html, "-o", out_file], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            success = os.path.exists(out_file) and os.path.getsize(out_file) > 0
        elif format == "epub":
            subprocess.run(["pandoc", in_html, "-o", out_file, "--metadata", f"title={clean_name}"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            success = os.path.exists(out_file) and os.path.getsize(out_file) > 0
        elif format == "pdf":
            try:
                import weasyprint
                weasyprint.HTML(in_html).write_pdf(out_file)
                success = os.path.exists(out_file) and os.path.getsize(out_file) > 0
            except Exception:
                subprocess.run(["libreoffice", "--headless", "--convert-to", "pdf", in_html, "--outdir", TEMP_DIR], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                gen_pdf = in_html.rsplit(".", 1)[0] + ".pdf"
                if os.path.exists(gen_pdf):
                    shutil.move(gen_pdf, out_file)
                success = os.path.exists(out_file) and os.path.getsize(out_file) > 0
    except Exception as e:
        logger.error(f"Create document error: {e}")

    if os.path.exists(in_html):
        try: os.remove(in_html)
        except: pass

    if success and os.path.exists(out_file) and os.path.getsize(out_file) > 0:
        background_tasks.add_task(cleanup_files, out_file)
        media_types = {
            'pdf': 'application/pdf',
            'docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            'epub': 'application/epub+zip',
            'txt': 'text/plain',
            'md': 'text/markdown',
            'html': 'text/html'
        }
        return FileResponse(
            out_file,
            media_type=media_types.get(format, 'application/octet-stream'),
            filename=f"{clean_name}.{format}"
        )
    else:
        if os.path.exists(out_file):
            try: os.remove(out_file)
            except: pass
        raise HTTPException(status_code=500, detail="Belge olusturulamadi.")
