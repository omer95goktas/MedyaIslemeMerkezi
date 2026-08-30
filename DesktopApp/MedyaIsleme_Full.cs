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
            this.Text = "Medya ve Belge İşleme Merkezi v1.0 - Windows";
            this.Size = new Size(800, 600);
            this.StartPosition = FormStartPosition.CenterScreen;

            InitializeMenu();

            TextBox txtWelcome = new TextBox();
            txtWelcome.Multiline = true;
            txtWelcome.ReadOnly = true;
            txtWelcome.Dock = DockStyle.Fill;
            txtWelcome.ScrollBars = ScrollBars.Vertical;
            txtWelcome.Font = new Font("Segoe UI", 12);
            txtWelcome.Text = 
                "\"Paylaşılmayan bilgi, bilgi değildir.\"\r\n" +
                "Medya ve Belge İşleme Merkezi'ne Hoş Geldiniz!\r\n" +
                "Ömer Göktaş tarafından geliştirilen bu sistem, ses, video ve belgelerinizi " +
                "tamamen erişilebilir bir şekilde yönetmenizi sağlar.\r\n" +
                "NASIL KULLANILIR?\r\n" +
                "Programın tüm özellikleri üst menü çubuğuna yerleştirilmiştir.\r\n" +
                "- Klavyenizdeki 'Alt' tuşuna basarak menüye odaklanabilirsiniz.\r\n" +
                "- Sağ ve sol ok tuşlarıyla 'Medya Araçları', 'Belge Araçları' ve 'Yardım' " +
                "sekmeleri arasında gezinebilirsiniz.\r\n" +
                "- İlgili menüyü açtıktan sonra ismin sonundaki harfe (örneğin S) basarak " +
                "aracı hızlıca çalıştırabilirsiniz.\r\n" +
                "MEDYA ARAÇLARI:\r\n" +
                "* Ses ve Video Formatı Dönüştürme\r\n" +
                "* Videodan Ses Çıkarma\r\n" +
                "* Sesten Video Oluşturma\r\n" +
                "* Ses veya Video Birleştirme\r\n" +
                "* Resim Formatı Dönüştürme\r\n" +
                "BELGE ARAÇLARI:\r\n" +
                "* PDF ve Görselden Metin Çıkarma (OCR)\r\n" +
                "* Belge Formatı Dönüştürme\r\n" +
                "* PDF Birleştirme ve PDF Bölme\r\n" +
                "İŞLEM SONUÇLARI NEREYE KAYDEDİLİR?\r\n" +
                "Yaptığınız tüm işlemler bilgisayarınızdaki 'Belgeler' klasörünün içine " +
                "otomatik olarak açılan 'Medya İşleme' klasörüne kaydedilir.\r\n" +
                "İyi kullanımlar!";

            txtWelcome.Select(0, 0); // Remove default selection
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
                        return true; // Eat escape, keep focus on this menu item
                    }
                }
                
                // If no dropdown is visible, but we are in the menu bar, just eat it.
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
                FileInputs = new List<FileInput> { new FileInput { Key = "audio", Label = "Ses Dosyası:", Filter = "Ses|*.mp3;*.wav;*.m4a;*.flac;*.aac;*.ogg|Tümü|*.*" } }
            }, "(&S)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Video Formatı Dönüştür", Endpoint = "/api/video-to-video", DefaultExtension = ".mp4", HasFormatSelect = true, Formats = new List<string>{"mp4", "mkv", "avi", "mov"},
                FileInputs = new List<FileInput> { new FileInput { Key = "video", Label = "Video Dosyası:", Filter = "Video|*.mp4;*.mkv;*.avi;*.mov|Tümü|*.*" } }
            }, "(&V)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Videodan Ses Çıkar", Endpoint = "/api/video-to-audio", DefaultExtension = ".mp3", HasFormatSelect = true, Formats = new List<string>{"mp3", "m4a", "m4r", "wav", "flac", "alac", "aiff", "opus", "ogg", "aac", "ac3", "wma", "mp2"},
                FileInputs = new List<FileInput> { new FileInput { Key = "video", Label = "Video Dosyası:", Filter = "Video|*.mp4;*.mkv;*.avi;*.mov|Tümü|*.*" } }
            }, "(&C)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Sesten Video Oluştur", Endpoint = "/api/audio-to-video", DefaultExtension = ".mp4", HasResolutionSelect = true,
                FileInputs = new List<FileInput> { 
                    new FileInput { Key = "audio", Label = "Ses Dosyası:", Filter = "Ses|*.mp3;*.wav;*.m4a;*.flac;*.aac;*.ogg|Tümü|*.*" },
                    new FileInput { Key = "image", Label = "Kapak Resmi:", Filter = "Resim|*.jpg;*.jpeg;*.png;*.webp|Tümü|*.*" }
                }
            }, "(&O)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Video Birleştir", Endpoint = "/api/merge-videos", DefaultExtension = ".mp4", HasResolutionSelect = true,
                FileInputs = new List<FileInput> { 
                    new FileInput { Key = "video1", Label = "1. Video:", Filter = "Video|*.mp4;*.mkv;*.avi;*.mov|Tümü|*.*" },
                    new FileInput { Key = "video2", Label = "2. Video:", Filter = "Video|*.mp4;*.mkv;*.avi;*.mov|Tümü|*.*" }
                }
            }, "(&B)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Ses Birleştir", Endpoint = "/api/merge-audios", DefaultExtension = ".mp3",
                FileInputs = new List<FileInput> { 
                    new FileInput { Key = "audio1", Label = "1. Ses:", Filter = "Ses|*.mp3;*.wav;*.m4a;*.flac;*.aac;*.ogg|Tümü|*.*" },
                    new FileInput { Key = "audio2", Label = "2. Ses:", Filter = "Ses|*.mp3;*.wav;*.m4a;*.flac;*.aac;*.ogg|Tümü|*.*" }
                }
            }, "(&I)");

            AddToolToMenu(menuMedya, new Tool { 
                Name = "Resim Formatı Dönüştür", Endpoint = "/api/image-to-image", DefaultExtension = ".png", HasFormatSelect = true, Formats = new List<string>{"jpg", "jpeg", "png", "webp", "ico", "pdf", "bmp", "tiff", "gif", "avif", "tga"},
                FileInputs = new List<FileInput> { new FileInput { Key = "image", Label = "Resim Dosyası:", Filter = "Resim|*.jpg;*.jpeg;*.png;*.webp|Tümü|*.*" } }
            }, "(&R)");

            // BELGE ARAÇLARI
            ToolStripMenuItem menuBelge = new ToolStripMenuItem("Belge Araçları (&B)");
            
            AddToolToMenu(menuBelge, new Tool { 
                Name = "Görsel ve PDF'den Metin Çıkar (OCR)", Endpoint = "/api/ocr-auto", DefaultExtension = ".txt", HasFormatSelect = true, Formats = new List<string>{"txt", "docx"},
                FileInputs = new List<FileInput> { new FileInput { Key = "document", Label = "Belge/Resim Dosyası:", Filter = "Belge|*.pdf;*.jpg;*.jpeg;*.png|Tümü|*.*" } }
            }, "(&G)");

            AddToolToMenu(menuBelge, new Tool { 
                Name = "Belge Formatı Dönüştür", Endpoint = "/api/convert-document", DefaultExtension = ".pdf", HasFormatSelect = true, Formats = new List<string>{"pdf", "docx", "md", "epub", "txt", "html", "odt", "rtf", "csv"},
                FileInputs = new List<FileInput> { new FileInput { Key = "file", Label = "Belge Dosyası:", Filter = "Belge|*.pdf;*.docx;*.txt;*.html;*.epub|Tümü|*.*" } }
            }, "(&D)");

            AddToolToMenu(menuBelge, new Tool { 
                Name = "PDF Böl", Endpoint = "/api/split-pdf", DefaultExtension = ".zip",
                FileInputs = new List<FileInput> { new FileInput { Key = "pdf_file", Label = "Bölünecek PDF Dosyası:", Filter = "PDF|*.pdf|Tümü|*.*" } }
            }, "(&L)");

            AddToolToMenu(menuBelge, new Tool { 
                Name = "PDF Birleştir", Endpoint = "/api/merge-pdf", DefaultExtension = ".pdf",
                FileInputs = new List<FileInput> { 
                    new FileInput { Key = "pdf1", Label = "1. PDF:", Filter = "PDF|*.pdf|Tümü|*.*" },
                    new FileInput { Key = "pdf2", Label = "2. PDF:", Filter = "PDF|*.pdf|Tümü|*.*" }
                }
            }, "(&P)");

            // YARDIM
            ToolStripMenuItem menuYardim = new ToolStripMenuItem("Yardım (&Y)");
            
            ToolStripMenuItem itemWebSitesi = new ToolStripMenuItem("Web Sitesi (&W)");
            itemWebSitesi.Click += (s, e) => { System.Diagnostics.Process.Start("https://omergoktas.net"); };
            
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
                               "Sürüm: 1.0.0\n" +
                               "Geliştirici: Ömer Göktaş\n\n" +
                               "Bu uygulama ses, video ve belgelerinizi tam erişilebilirlikle " +
                               "yönetebilmeniz için özel olarak tasarlanmıştır.\n\n" +
                               "Ekran okuyucularla %100 uyumludur.";
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
        private Timer progressTimer;

        public ToolForm(Tool t)
        {
            this.tool = t;
            this.Text = t.Name;
            this.Size = new Size(500, 500);
            this.StartPosition = FormStartPosition.CenterParent; this.KeyPreview = true; this.KeyDown += (senderForm, eventArgs) => { if (eventArgs.KeyCode == Keys.Escape) this.Close(); };

            FlowLayoutPanel panel = new FlowLayoutPanel();
            panel.Dock = DockStyle.Fill;
            panel.FlowDirection = FlowDirection.TopDown;
            panel.WrapContents = false;
            panel.Padding = new Padding(10);
            panel.AutoScroll = true;

            foreach (var input in t.FileInputs)
            {
                AddFilePicker(panel, input.Label, input.Filter, path => selectedFiles[input.Key] = path);
            }

            if (t.HasFormatSelect)
            {
                Label lbl = new Label { Text = "Hedef Format:", AutoSize = true, Margin = new Padding(0, 10, 0, 5) };
                comboFormat = new ComboBox { DropDownStyle = ComboBoxStyle.DropDownList, Width = 200 };
                foreach (var f in t.Formats) comboFormat.Items.Add(f);
                comboFormat.SelectedIndex = 0;
                panel.Controls.Add(lbl);
                panel.Controls.Add(comboFormat);
            }

            if (t.HasResolutionSelect)
            {
                Label lbl = new Label { Text = "Çözünürlük (Örn: 1080p_vertical, 1080p_horizontal):", AutoSize = true, Margin = new Padding(0, 10, 0, 5) };
                comboResolution = new ComboBox { DropDownStyle = ComboBoxStyle.DropDownList, Width = 200 };
                comboResolution.Items.Add("1080p Yatay");
                comboResolution.Items.Add("1080p Dikey");
                comboResolution.Items.Add("720p Yatay");
                comboResolution.Items.Add("720p Dikey");
                comboResolution.SelectedIndex = 0;
                panel.Controls.Add(lbl);
                panel.Controls.Add(comboResolution);
            }

            progressBar = new ProgressBar { Width = 400, Height = 25, Margin = new Padding(0, 20, 0, 10), Style = ProgressBarStyle.Continuous };
            panel.Controls.Add(progressBar);

            btnStart = new Button { Text = "İşlemi Başlat", Width = 150, Height = 40 };
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

            txtStatus = new TextBox { Width = 400, Height = 50, Multiline = true, ReadOnly = true, TabStop = true, Text = "Durum: İşlem bekleniyor...", Margin = new Padding(0, 15, 0, 0) };
            panel.Controls.Add(txtStatus);

            this.Controls.Add(panel);
        }

        private void AddFilePicker(FlowLayoutPanel panel, string labelText, string filter, Action<string> onSelected)
        {
            Label lbl = new Label { Text = labelText, AutoSize = true, Margin = new Padding(0, 10, 0, 5) };
            TextBox txt = new TextBox { Width = 300, ReadOnly = true, TabStop = true };
            Button btn = new Button { Text = "Gözat...", Width = 80 };
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
            progressBar.Style = ProgressBarStyle.Marquee;
            
            try
            {
                using (HttpClient client = new HttpClient())
                {
                    client.Timeout = TimeSpan.FromMinutes(20); // Long timeout for big conversions
                    using (MultipartFormDataContent content = new MultipartFormDataContent())
                    {
                        foreach (var input in tool.FileInputs)
                        {
                            if (!selectedFiles.ContainsKey(input.Key) || string.IsNullOrEmpty(selectedFiles[input.Key]))
                            {
                                throw new Exception("Lütfen tüm dosyaları seçin: " + input.Label);
                            }
                            var path = selectedFiles[input.Key];
                            var fs = new FileStream(path, FileMode.Open, FileAccess.Read);
                            var sc = new StreamContent(fs);
                            sc.Headers.ContentType = MediaTypeHeaderValue.Parse("application/octet-stream");
                            if (tool.Endpoint == "/api/ocr-auto" && input.Key == "document") {
                                // Skip adding it with the wrong key, the OCR block below will handle it
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
                                var fs2 = new FileStream(selectedFiles["document"], FileMode.Open, FileAccess.Read);
                                var sc2 = new StreamContent(fs2);
                                sc2.Headers.ContentType = MediaTypeHeaderValue.Parse("application/octet-stream");
                                content.Add(sc2, "pdf_file", Path.GetFileName(selectedFiles["document"]));
                            } else {
                                url = "https://medya.omergoktas.net/api/image-to-text";
                                var fs2 = new FileStream(selectedFiles["document"], FileMode.Open, FileAccess.Read);
                                var sc2 = new StreamContent(fs2);
                                sc2.Headers.ContentType = MediaTypeHeaderValue.Parse("application/octet-stream");
                                content.Add(sc2, "file", Path.GetFileName(selectedFiles["document"]));
                            }
                        }

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
                            MessageBox.Show("İşlem başarıyla tamamlandı!\nDosya kaydedildi:\n" + fullOutPath, "Başarılı", MessageBoxButtons.OK, MessageBoxIcon.Information);
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
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
        }
    }
}
