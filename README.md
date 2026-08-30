# Medya ve Belge İşleme Merkezi

Bu proje, görme engelli kullanıcıların tamamen erişilebilir bir şekilde kullanabilmesi için geliştirilmiş çok amaçlı bir medya ve belge dönüştürme/işleme sistemidir. Proje, web tabanlı bir arayüz, güçlü bir Python/FastAPI arka ucu ve NVDA/JAWS ekran okuyucularla tam uyumlu çalışan bir C# (WinForms) Windows masaüstü istemcisi içermektedir.

## Özellikler
- **Ses Araçları:** Format dönüştürme, birleştirme.
- **Video Araçları:** Format dönüştürme, videodan sese, sesten videoya, video birleştirme.
- **Belge Araçları:** OCR (Metin çıkarma), format dönüştürme (PDF, Word, TXT vb.), PDF bölme/birleştirme.
- **Resim Araçları:** Format dönüştürme.
- **Erişilebilirlik:** Windows uygulaması NVDA ve JAWS ile %100 uyumludur. Uzun süren işlemlerde kullanıcıyı SAPI (sesli asistan) ile sesli olarak uyarır ve bilgi verir.

## Mimari
- **Backend:** Python (FastAPI), FFMPEG, LibreOffice, Tesseract-OCR.
- **Frontend:** HTML, CSS, JavaScript (Responsive ve Erişilebilir).
- **Desktop Client:** C# .NET Framework (WinForms), HttpClient.
