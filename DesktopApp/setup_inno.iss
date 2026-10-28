; ===================================================================
; Medya ve Belge İşleme Merkezi - Resmi Inno Setup Kurulum Senaryosu
; Standart, güvenli, %100 erişilebilir Windows kurulum paketi
; ===================================================================

#define MyAppName "Medya ve Belge İşleme Merkezi"
#define MyAppVersion "1.0.1"
#define MyAppPublisher "omergoktas.net"
#define MyAppURL "https://omergoktas.net"
#define MyAppExeName "MedyaIslemeMerkezi.exe"

[Setup]
; Temel Uygulama Tanımlamaları
AppId={{D37F28B1-4A59-4D8A-9A8C-B2456CE98451}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}

; Yönetici yetkisi ile Program Files kurulumu
PrivilegesRequired=admin
DefaultDirName={autopf}\Omer_Goktas_Net\Medya ve Belge Isleme Merkezi
DefaultGroupName=Omer_Goktas_Net
DisableProgramGroupPage=yes

; Çıktı Ayarları
OutputDir=..\release_artifacts
OutputBaseFilename=MedyaIslemeMerkezi
Compression=lzma2/max
SolidCompression=yes

; Görünüm ve Erişilebilirlik
WizardStyle=modern
SetupIconFile=app.ico
DisableDirPage=yes
DisableReadyPage=yes
AlwaysShowDirOnReadyPage=no
CloseApplications=yes
RestartApplications=no
ArchitecturesInstallIn64BitMode=x64compatible

; Türkçe Dil Desteği
[Languages]
Name: "turkish"; MessagesFile: "compiler:Languages\Turkish.isl"

[Tasks]
Name: "desktopicon"; Description: "Masaüstü kısayolu oluştur"; GroupDescription: "Ek Kısayollar:"

[Files]
; Derlenmiş uygulamanın tüm dosyalarını dahil et
Source: "..\publish_folder\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"
Name: "{group}\{#MyAppName} Kaldır"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
; Kurulum bittiğinde başlatma seçeneği
Filename: "{app}\{#MyAppExeName}"; Description: "{#MyAppName} uygulamasını başlat"; Flags: nowait postinstall skipifsilent

[Code]
// Kurulum başlamadan önce sadece kurulu olan eski uygulamayı kapat (kurulum sihirbazını kapatma)
procedure CurStepChanged(CurStep: TSetupStep);
var
  ResultCode: Integer;
begin
  if CurStep = ssInstall then
  begin
    Exec('powershell.exe', '-NoProfile -NonInteractive -Command "try { Get-Process -Name MedyaIslemeMerkezi -ErrorAction SilentlyContinue | Where-Object { $_.Path -like ''*Omer_Goktas_Net*'' } | Stop-Process -Force } catch {} ; exit 0"', '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
    Sleep(500);
  end;
end;
