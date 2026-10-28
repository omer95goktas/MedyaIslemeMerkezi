using System;
using System.IO;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Drawing;
using System.Collections.Generic;

namespace MedyaIslemeMerkezi
{
    public class FileInput
    {
        public string Key { get; set; }
        public string Label { get; set; }
        public string Filter { get; set; }
    }

    public class Tool
    {
        public string Name { get; set; }
        public string Endpoint { get; set; }
        public List<FileInput> FileInputs { get; set; }
        public bool HasFormatSelect { get; set; }
        public List<string> Formats { get; set; }
        public bool HasResolutionSelect { get; set; }
        public string DefaultExtension { get; set; }
    }

    public class MainForm : Form
    {
        public MainForm()
        {
            this.Text = "Medya ve Belge İşleme Merkezi v1.0.1 - Windows";
            this.Size = new Size(820, 620);
            this.StartPosition = FormStartPosition.CenterScreen;

            InitializeMenu();

            TextBox txtWelcome = new TextBox();
            txtWelcome.Multiline = true;
            txtWelcome.ReadOnly = true;
            txtWelcome.Dock = DockStyle.Fill;
            txtWelcome.ScrollBars = ScrollBars.Vertical;
            txtWelcome.Font = new Font("Segoe UI", 12);
            txtWelcome.Text = 
                "\"Paylaşılmayan bilgi, bilgi değildir.\"\r\n\r\n" +
                "Medya ve Belge İşleme Merkezi'ne Hoş Geldiniz!\r\n\r\n" +
                "Ömer Göktaş (omergoktas.net) tarafından geliştirilen bu sistem; ses, video ve belgelerinizi " +
                "tamamen erişilebilir bir şekilde dönüştürmenizi, düzenlemenizi ve yönetmenizi sağlar.\r\n\r\n" +
                "NASIL KULLANILIR?\r\n" +
                "Programın tüm özellikleri üst menü çubuğuna yerleştirilmiştir.\r\n" +
                "- Klavyenizdeki 'Alt' tuşuna basarak üst menüye odaklanabilirsiniz.\r\n" +
                "- Sağ ve sol ok tuşlarıyla 'Dosya', 'Medya Araçları', 'Belge Araçları' ve 'Yardım' " +
                "sekmeleri arasında gezinebilirsiniz.\r\n" +
                "- İlgili menüyü açtıktan sonra listeden istediğiniz aracı seçebilir veya kısayol harfine basarak " +
                "aracı anında başlatabilirsiniz.\r\n\r\n" +
                "MEDYA ARAÇLARI:\r\n" +
                "* Ses Formatı Dönüştür (MP3, WAV, AAC, M4A, FLAC, OPUS, OGG...)\r\n" +
                "* Video Formatı Dönüştür (MP4, MKV, AVI, MOV, WEBM, FLV, WMV...)\r\n" +
                "* Videodan Ses Çıkar (Videodan yüksek kaliteli MP3/WAV ses ayıklama)\r\n" +
                "* Sesten Video Oluştur (Sese kapak resmi ekleyerek video yapma)\r\n" +
                "* Video Birleştir ve Ses Birleştir\r\n" +
                "* Resim Formatı Dönüştür (JPG, PNG, WEBP, ICO, PDF, BMP...)\r\n\r\n" +
                "BELGE ARAÇLARI:\r\n" +
                "* Görsel ve PDF'den Metin Çıkar (OCR / Taranmış belgeleri metne dökme)\r\n" +
                "* Belge Formatı Dönüştür (PDF, Word DOCX, EPUB, TXT, HTML, ODT...)\r\n" +
                "* PDF Böl ve PDF Birleştir\r\n\r\n" +
                "İŞLEM SONUÇLARI NEREYE KAYDEDİLİR?\r\n" +
                "Yaptığınız tüm işlemler bilgisayarınızdaki 'Belgeler' klasörünün içine " +
                "otomatik olarak açılan 'Medya İşleme' klasörüne güvenle kaydedilir.\r\n\r\n" +
                "İyi kullanımlar!";

            txtWelcome.Select(0, 0);
            this.Controls.Add(txtWelcome);
        }

        protected override bool ProcessCmdKey(ref Message msg, Keys keyData)
        {
            if (keyData == Keys.Escape && this.MainMenuStrip != null)
            {
                foreach (ToolStripMenuItem item in this.MainMenuStrip.Items)
                {
                    if (item.DropDown.Visible)
                    {
                        item.DropDown.Close();
                        item.Select();
                        return true;
                    }
                }
                
                if (this.MainMenuStrip.Focused || this.MainMenuStrip.ContainsFocus)
                {
                    return true;
                }
            }
            return base.ProcessCmdKey(ref msg, keyData);
        }

        private void InitializeMenu()
        {
            MenuStrip menu = new MenuStrip();
            
            // DOSYA
            ToolStripMenuItem menuDosya = new ToolStripMenuItem("Dosya (&D)");
            ToolStripMenuItem itemCikis = new ToolStripMenuItem("Çıkış (&X)");
            itemCikis.Click += (s, e) => this.Close();
            menuDosya.DropDownItems.Add(itemCikis);

            // MEDYA ARAÇLARI
            ToolStripMenuItem menuMedya = new ToolStripMenuItem("Medya Araçları (&M)");
            
            AddToolToMenu(menuMedya, new Tool { 
                Name = "Ses Formatı Dönüştür", Endpoint = "/api/audio-to-audio", DefaultExtension = ".mp3", HasFormatSelect = true, Formats = new List<string>{"mp3", "m4a", "m4r", "wav", "flac", "alac", "aiff", "opus", "ogg", "aac", "ac3", "wma", "mp2"},
                FileInputs = new List<FileInput> { new FileInput { Key = "audio", Label = "Ses Dosyası:", Filter = "Ses Dosyaları|*.mp3;*.wav;*.m4a;*.flac;*.aac;*.ogg;*.opus;*.wma|Tüm Dosyalar|*.*" } }
            }, "(&S)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Video Formatı Dönüştür", Endpoint = "/api/video-to-video", DefaultExtension = ".mp4", HasFormatSelect = true, Formats = new List<string>{"mp4", "avi", "mov", "mkv", "flv", "wmv", "webm", "m4v", "mpeg", "3gp", "ts", "vob", "ogv"},
                FileInputs = new List<FileInput> { new FileInput { Key = "video", Label = "Video Dosyası:", Filter = "Video Dosyaları|*.mp4;*.mkv;*.avi;*.mov;*.flv;*.wmv;*.webm;*.m4v;*.mpeg;*.3gp;*.ts;*.vob;*.ogv|Tüm Dosyalar|*.*" } }
            }, "(&V)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Videodan Ses Çıkar", Endpoint = "/api/video-to-audio", DefaultExtension = ".mp3", HasFormatSelect = true, Formats = new List<string>{"mp3", "m4a", "m4r", "wav", "flac", "alac", "aiff", "opus", "ogg", "aac", "ac3", "wma", "mp2"},
                FileInputs = new List<FileInput> { new FileInput { Key = "video", Label = "Video Dosyası:", Filter = "Video Dosyaları|*.mp4;*.mkv;*.avi;*.mov;*.flv;*.wmv;*.webm;*.m4v;*.mpeg;*.3gp;*.ts;*.vob;*.ogv|Tüm Dosyalar|*.*" } }
            }, "(&C)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Sesten Video Oluştur", Endpoint = "/api/audio-to-video", DefaultExtension = ".mp4", HasResolutionSelect = true,
                FileInputs = new List<FileInput> { 
                    new FileInput { Key = "audio", Label = "Ses Dosyası:", Filter = "Ses Dosyaları|*.mp3;*.wav;*.m4a;*.flac;*.aac;*.ogg;*.opus;*.wma|Tüm Dosyalar|*.*" },
                    new FileInput { Key = "image", Label = "Kapak Resmi:", Filter = "Resim Dosyaları|*.jpg;*.jpeg;*.png;*.webp;*.bmp|Tüm Dosyalar|*.*" }
                }
            }, "(&O)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Video Birleştir", Endpoint = "/api/merge-videos", DefaultExtension = ".mp4", HasResolutionSelect = true,
                FileInputs = new List<FileInput> { 
                    new FileInput { Key = "video1", Label = "1. Video:", Filter = "Video Dosyaları|*.mp4;*.mkv;*.avi;*.mov;*.flv;*.wmv;*.webm;*.m4v;*.mpeg;*.3gp;*.ts;*.vob;*.ogv|Tüm Dosyalar|*.*" },
                    new FileInput { Key = "video2", Label = "2. Video:", Filter = "Video Dosyaları|*.mp4;*.mkv;*.avi;*.mov;*.flv;*.wmv;*.webm;*.m4v;*.mpeg;*.3gp;*.ts;*.vob;*.ogv|Tüm Dosyalar|*.*" }
                }
            }, "(&B)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Ses Birleştir", Endpoint = "/api/merge-audios", DefaultExtension = ".mp3",
                FileInputs = new List<FileInput> { 
                    new FileInput { Key = "audio1", Label = "1. Ses:", Filter = "Ses Dosyaları|*.mp3;*.wav;*.m4a;*.flac;*.aac;*.ogg;*.opus;*.wma|Tüm Dosyalar|*.*" },
                    new FileInput { Key = "audio2", Label = "2. Ses:", Filter = "Ses Dosyaları|*.mp3;*.wav;*.m4a;*.flac;*.aac;*.ogg;*.opus;*.wma|Tüm Dosyalar|*.*" }
                }
            }, "(&I)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Resim Formatı Dönüştür", Endpoint = "/api/image-to-image", DefaultExtension = ".png", HasFormatSelect = true, Formats = new List<string>{"jpg", "jpeg", "png", "webp", "ico", "pdf", "bmp", "tiff", "gif", "avif", "tga"},
                FileInputs = new List<FileInput> { new FileInput { Key = "image", Label = "Resim Dosyası:", Filter = "Resim Dosyaları|*.jpg;*.jpeg;*.png;*.webp;*.bmp;*.gif;*.tiff|Tüm Dosyalar|*.*" } }
            }, "(&R)");

            // BELGE ARAÇLARI
            ToolStripMenuItem menuBelge = new ToolStripMenuItem("Belge Araçları (&B)");
            
            AddToolToMenu(menuBelge, new Tool { 
                Name = "Görsel ve PDF'den Metin Çıkar (OCR)", Endpoint = "/api/ocr-auto", DefaultExtension = ".txt", HasFormatSelect = true, Formats = new List<string>{"txt", "docx"},
                FileInputs = new List<FileInput> { new FileInput { Key = "document", Label = "Belge veya Görsel Dosyası:", Filter = "Belge/Görsel|*.pdf;*.jpg;*.jpeg;*.png;*.webp;*.bmp|Tüm Dosyalar|*.*" } }
            }, "(&G)");

            AddToolToMenu(menuBelge, new Tool { 
                Name = "Belge Formatı Dönüştür", Endpoint = "/api/convert-document", DefaultExtension = ".pdf", HasFormatSelect = true, Formats = new List<string>{"pdf", "docx", "md", "epub", "txt", "html", "odt", "rtf", "csv"},
                FileInputs = new List<FileInput> { new FileInput { Key = "file", Label = "Belge Dosyası:", Filter = "Belge Dosyaları|*.pdf;*.docx;*.doc;*.txt;*.html;*.epub;*.odt;*.rtf;*.md|Tüm Dosyalar|*.*" } }
            }, "(&D)");

            AddToolToMenu(menuBelge, new Tool { 
                Name = "PDF Böl", Endpoint = "/api/split-pdf", DefaultExtension = ".zip",
                FileInputs = new List<FileInput> { new FileInput { Key = "pdf_file", Label = "Bölünecek PDF Dosyası:", Filter = "PDF Dosyaları|*.pdf|Tüm Dosyalar|*.*" } }
            }, "(&L)");

            AddToolToMenu(menuBelge, new Tool { 
                Name = "PDF Birleştir", Endpoint = "/api/merge-pdf", DefaultExtension = ".pdf",
                FileInputs = new List<FileInput> { 
                    new FileInput { Key = "pdf1", Label = "1. PDF Dosyası:", Filter = "PDF Dosyaları|*.pdf|Tüm Dosyalar|*.*" },
                    new FileInput { Key = "pdf2", Label = "2. PDF Dosyası:", Filter = "PDF Dosyaları|*.pdf|Tüm Dosyalar|*.*" }
                }
            }, "(&P)");

            // YARDIM
            ToolStripMenuItem menuYardim = new ToolStripMenuItem("Yardım (&Y)");
            
            ToolStripMenuItem itemWebSitesi = new ToolStripMenuItem("Web Sitesi (&W)");
            itemWebSitesi.Click += (s, e) => { 
                try { System.Diagnostics.Process.Start("https://omergoktas.net"); } catch {}
            };
            
            ToolStripMenuItem itemHakkinda = new ToolStripMenuItem("Hakkında (&H)");
            itemHakkinda.Click += (s, e) => ShowAbout();
            
            menuYardim.DropDownItems.Add(itemWebSitesi);
            menuYardim.DropDownItems.Add(itemHakkinda);

            menu.Items.Add(menuDosya);
            menu.Items.Add(menuMedya);
            menu.Items.Add(menuBelge);
            menu.Items.Add(menuYardim);
            this.MainMenuStrip = menu;
            this.Controls.Add(menu); 
        }

        private void AddToolToMenu(ToolStripMenuItem menu, Tool tool, string shortcut)
        {
            var item = new ToolStripMenuItem(tool.Name + " " + shortcut);
            item.Click += (s, e) => { new ToolForm(tool).ShowDialog(); this.Focus(); menu.ShowDropDown(); item.Select(); };
            menu.DropDownItems.Add(item);
        }

        private void ShowAbout()
        {
            string aboutText = "Medya ve Belge İşleme Merkezi - Windows Sürümü\n\n" +
                               "Sürüm: 1.0.1\n" +
                               "Geliştirici: Ömer Göktaş (omergoktas.net)\n\n" +
                               "Bu uygulama ses, video ve belgelerinizi tam erişilebilirlikle " +
                               "yönetebilmeniz ve dönüştürebilmeniz için özel olarak geliştirilmiştir.\n\n" +
                               "NVDA ve JAWS gibi ekran okuyucularla %100 uyumludur.";
            MessageBox.Show(aboutText, "Hakkında", MessageBoxButtons.OK, MessageBoxIcon.Information);
        }
    }

    public class ToolForm : Form
    {
        private Tool tool;
        private Dictionary<string, string> selectedFiles = new Dictionary<string, string>();
        
        private ComboBox comboFormat;
        private ComboBox comboResolution;
        private ProgressBar progressBar;
        private Button btnStart;
        private TextBox txtStatus;
        private Label lblProgress;
        private Timer progressTimer;

        public ToolForm(Tool t)
        {
            this.tool = t;
            this.Text = t.Name;
            this.Size = new Size(520, 520);
            this.StartPosition = FormStartPosition.CenterParent;
            this.KeyPreview = true;
            this.KeyDown += (senderForm, eventArgs) => { if (eventArgs.KeyCode == Keys.Escape) this.Close(); };

            FlowLayoutPanel panel = new FlowLayoutPanel();
            panel.Dock = DockStyle.Fill;
            panel.FlowDirection = FlowDirection.TopDown;
            panel.WrapContents = false;
            panel.Padding = new Padding(12);
            panel.AutoScroll = true;

            foreach (var input in t.FileInputs)
            {
                AddFilePicker(panel, input.Label, input.Filter, path => selectedFiles[input.Key] = path);
            }

            if (t.HasFormatSelect)
            {
                Label lbl = new Label { Text = "Hedef Format:", AutoSize = true, Margin = new Padding(0, 10, 0, 5) };
                comboFormat = new ComboBox { DropDownStyle = ComboBoxStyle.DropDownList, Width = 220 };
                foreach (var f in t.Formats) comboFormat.Items.Add(f);
                comboFormat.SelectedIndex = 0;
                panel.Controls.Add(lbl);
                panel.Controls.Add(comboFormat);
            }

            if (t.HasResolutionSelect)
            {
                Label lbl = new Label { Text = "Çözünürlük Seçeneği:", AutoSize = true, Margin = new Padding(0, 10, 0, 5) };
                comboResolution = new ComboBox { DropDownStyle = ComboBoxStyle.DropDownList, Width = 220 };
                comboResolution.Items.Add("1080p Yatay");
                comboResolution.Items.Add("1080p Dikey");
                comboResolution.Items.Add("720p Yatay");
                comboResolution.Items.Add("720p Dikey");
                comboResolution.SelectedIndex = 0;
                panel.Controls.Add(lbl);
                panel.Controls.Add(comboResolution);
            }

            progressBar = new ProgressBar { Width = 460, Height = 26, Margin = new Padding(0, 20, 0, 10), Style = ProgressBarStyle.Continuous };
            lblProgress = new Label { Text = "Hazır", AutoSize = true, Margin = new Padding(0, 0, 0, 15), Font = new Font("Segoe UI", 10, FontStyle.Bold) };
            panel.Controls.Add(lblProgress);
            panel.Controls.Add(progressBar);

            btnStart = new Button { Text = "İşlemi Başlat", Width = 160, Height = 42, Font = new Font("Segoe UI", 10, FontStyle.Bold) };
            btnStart.Click += async (s, e) => await StartProcess();
            panel.Controls.Add(btnStart);

            progressTimer = new Timer { Interval = 500 };
            progressTimer.Tick += (s, e) => {
                if (progressBar.Value < 95) {
                    progressBar.Value += 1;
                    if (progressBar.Value == 25) {
                        try { Task.Run(() => { dynamic synth = Activator.CreateInstance(Type.GetTypeFromProgID("SAPI.SpVoice")); synth.Speak("Yüzde 25"); }); } catch {}
                    } else if (progressBar.Value == 50) {
                        try { Task.Run(() => { dynamic synth = Activator.CreateInstance(Type.GetTypeFromProgID("SAPI.SpVoice")); synth.Speak("Yüzde 50"); }); } catch {}
                    } else if (progressBar.Value == 75) {
                        try { Task.Run(() => { dynamic synth = Activator.CreateInstance(Type.GetTypeFromProgID("SAPI.SpVoice")); synth.Speak("Yüzde 75"); }); } catch {}
                    }
                }
            };

            txtStatus = new TextBox { Width = 460, Height = 60, Multiline = true, ReadOnly = true, TabStop = true, Text = "Durum: İşlem bekleniyor...", Margin = new Padding(0, 15, 0, 0) };
            panel.Controls.Add(txtStatus);

            this.Controls.Add(panel);
        }

        private void AddFilePicker(FlowLayoutPanel panel, string labelText, string filter, Action<string> onSelected)
        {
            Label lbl = new Label { Text = labelText, AutoSize = true, Margin = new Padding(0, 10, 0, 5) };
            TextBox txt = new TextBox { Width = 340, ReadOnly = true, TabStop = true };
            Button btn = new Button { Text = "Gözat...", Width = 100, Height = 28 };
            btn.Click += (s, e) =>
            {
                OpenFileDialog ofd = new OpenFileDialog { Filter = filter };
                if (ofd.ShowDialog() == DialogResult.OK)
                {
                    txt.Text = ofd.FileName;
                    onSelected(ofd.FileName);
                }
            };

            FlowLayoutPanel row = new FlowLayoutPanel { FlowDirection = FlowDirection.LeftToRight, AutoSize = true };
            row.Controls.Add(txt);
            row.Controls.Add(btn);

            panel.Controls.Add(lbl);
            panel.Controls.Add(row);
        }

        private async Task StartProcess()
        {
            btnStart.Enabled = false;
            progressBar.Style = ProgressBarStyle.Continuous;
            lblProgress.Text = "Yükleniyor...";
            txtStatus.Text = "Durum: Dosyalar sunucuya aktarılıyor...";
            
            try
            {
                using (var handler = new HttpClientHandler { UseProxy = false, Proxy = null })
                using (HttpClient client = new HttpClient(handler))
                {
                    client.Timeout = TimeSpan.FromMinutes(60);
                    using (MultipartFormDataContent content = new MultipartFormDataContent())
                    {
                        foreach (var input in tool.FileInputs)
                        {
                            if (!selectedFiles.ContainsKey(input.Key) || string.IsNullOrEmpty(selectedFiles[input.Key]))
                            {
                                throw new Exception("Lütfen tüm gerekli dosyaları seçin: " + input.Label);
                            }
                            var path = selectedFiles[input.Key];
                            var fs = new FileStream(path, FileMode.Open, FileAccess.Read, FileShare.Read, 1048576, true);
                            var sc = new EngelsizMedya.ProgressStreamContent(fs, 1048576, (u, t) => {
                                int p = (int)((u * 100) / t);
                                this.Invoke((System.Windows.Forms.MethodInvoker)delegate {
                                    if (p > 100) p = 100;
                                    progressBar.Value = p;
                                    lblProgress.Text = "Yükleniyor: " + (u / 1048576) + " MB / " + (t / 1048576) + " MB (%" + p + ")";
                                });
                            });
                            sc.Headers.ContentType = MediaTypeHeaderValue.Parse("application/octet-stream");
                            if (tool.Endpoint == "/api/ocr-auto" && input.Key == "document") {
                                // Handled below in specialized OCR block
                            } else {
                                content.Add(sc, input.Key, Path.GetFileName(path));
                            }
                        }

                        if (tool.HasFormatSelect)
                        {
                            content.Add(new StringContent(comboFormat.SelectedItem.ToString()), "format");
                        }

                        if (tool.HasResolutionSelect)
                        {
                            content.Add(new StringContent(comboResolution.SelectedItem.ToString()), "resolution");
                        }

                        string url = "https://medya.omergoktas.net" + tool.Endpoint;
                        if (tool.Endpoint == "/api/ocr-auto") {
                            var docPath = selectedFiles["document"].ToLower();
                            if (docPath.EndsWith(".pdf")) {
                                url = "https://medya.omergoktas.net/api/pdf-ocr";
                                var fs2 = new FileStream(selectedFiles["document"], FileMode.Open, FileAccess.Read, FileShare.Read, 1048576, true);
                                var sc2 = new EngelsizMedya.ProgressStreamContent(fs2, 1048576, (u, t) => {
                                    int p = (int)((u * 100) / t);
                                    this.Invoke((System.Windows.Forms.MethodInvoker)delegate {
                                        if (p > 100) p = 100;
                                        progressBar.Value = p;
                                        lblProgress.Text = "Yükleniyor: " + (u / 1048576) + " MB / " + (t / 1048576) + " MB (%" + p + ")";
                                    });
                                });
                                sc2.Headers.ContentType = MediaTypeHeaderValue.Parse("application/octet-stream");
                                content.Add(sc2, "pdf_file", Path.GetFileName(selectedFiles["document"]));
                            } else {
                                url = "https://medya.omergoktas.net/api/image-to-text";
                                var fs2 = new FileStream(selectedFiles["document"], FileMode.Open, FileAccess.Read, FileShare.Read, 1048576, true);
                                var sc2 = new EngelsizMedya.ProgressStreamContent(fs2, 1048576, (u, t) => {
                                    int p = (int)((u * 100) / t);
                                    this.Invoke((System.Windows.Forms.MethodInvoker)delegate {
                                        if (p > 100) p = 100;
                                        progressBar.Value = p;
                                        lblProgress.Text = "Yükleniyor: " + (u / 1048576) + " MB / " + (t / 1048576) + " MB (%" + p + ")";
                                    });
                                });
                                sc2.Headers.ContentType = MediaTypeHeaderValue.Parse("application/octet-stream");
                                content.Add(sc2, "file", Path.GetFileName(selectedFiles["document"]));
                            }
                        }

                        txtStatus.Text = "Durum: Sunucu dosyayı işliyor, lütfen bekleyin...";
                        HttpResponseMessage response = await client.PostAsync(url, content);

                        if (response.IsSuccessStatusCode)
                        {
                            string docs = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
                            string outDir = Path.Combine(docs, "Medya İşleme");
                            if (!Directory.Exists(outDir)) Directory.CreateDirectory(outDir);

                            long timestamp = (long)(DateTime.UtcNow.Subtract(new DateTime(1970, 1, 1))).TotalSeconds;
                            
                            string ext = tool.DefaultExtension;
                            if (tool.HasFormatSelect) ext = "." + comboFormat.SelectedItem.ToString();
                            
                            string outName = "omer_goktas_net_" + timestamp + ext;
                            string fullOutPath = Path.Combine(outDir, outName);

                            using (var fs = new FileStream(fullOutPath, FileMode.Create, FileAccess.Write))
                            {
                                await response.Content.CopyToAsync(fs);
                            }

                            progressBar.Style = ProgressBarStyle.Continuous;
                            progressBar.Value = 100;
                            lblProgress.Text = "Tamamlandı";
                            txtStatus.Text = "Durum: İşlem başarıyla tamamlandı!";
                            try { Task.Run(() => { dynamic synth = Activator.CreateInstance(Type.GetTypeFromProgID("SAPI.SpVoice")); synth.Speak("İşlem tamamlandı"); }); } catch {}
                            MessageBox.Show("İşlem başarıyla tamamlandı!\n\nKaydedilen dosya:\n" + fullOutPath, "Başarılı - omergoktas.net", MessageBoxButtons.OK, MessageBoxIcon.Information);
                            this.Close();
                        }
                        else
                        {
                            string err = await response.Content.ReadAsStringAsync();
                            throw new Exception(string.Format("Sunucu Hatası ({0}): {1}", response.StatusCode, err));
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                progressBar.Style = ProgressBarStyle.Continuous;
                progressBar.Value = 0;
                lblProgress.Text = "Hata";
                txtStatus.Text = "Hata: " + ex.Message;
                MessageBox.Show(ex.Message, "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                btnStart.Enabled = true;
            }
        }
    }

    static class Program
    {
        [STAThread]
        static void Main()
        {
            System.Net.ServicePointManager.SecurityProtocol = (System.Net.SecurityProtocolType)3072;
            System.Net.ServicePointManager.Expect100Continue = false;
            System.Net.ServicePointManager.DefaultConnectionLimit = 50;
            System.Net.ServicePointManager.UseNagleAlgorithm = false;
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
        }
    }
}
