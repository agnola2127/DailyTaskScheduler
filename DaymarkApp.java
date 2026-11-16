import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class DaymarkApp {

    private static final int PORT = 8080;
    private static Path dataFile;

    public static void main(String[] args) throws Exception {
        dataFile = Paths.get(System.getProperty("user.dir"), "daymark-data.json");

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        server.createContext("/", DaymarkApp::handleIndex);
        server.createContext("/api/data", DaymarkApp::handleData);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        String url = "http://127.0.0.1:" + PORT + "/";
        System.out.println("--------------------------------------------------");
        System.out.println("  Daymark is running at: " + url);
        System.out.println("  Data file           : " + dataFile.toAbsolutePath());
        System.out.println("  Stop with Ctrl + C");
        System.out.println("--------------------------------------------------");
        
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception e) {
            System.out.println("Could not open browser automatically. Please visit the URL above.");
        }
    }

    private static void handleIndex(HttpExchange ex) throws IOException {
        if (!ex.getRequestURI().getPath().equals("/")) {
            send(ex, 404, "text/plain; charset=utf-8", "Not found");
            return;
        }
        send(ex, 200, "text/html; charset=utf-8", HTML);
    }

    private static void handleData(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            String body = "{}";
            if (Files.exists(dataFile)) {
                body = Files.readString(dataFile, StandardCharsets.UTF_8);
                if (body.isBlank()) body = "{}";
            }
            send(ex, 200, "application/json; charset=utf-8", body);
            return;
        }
        if ("POST".equalsIgnoreCase(method)) {
            byte[] raw = ex.getRequestBody().readAllBytes();
            String body = new String(raw, StandardCharsets.UTF_8);
            Files.writeString(dataFile, body, StandardCharsets.UTF_8);
            send(ex, 200, "application/json; charset=utf-8", "{\"ok\":true}");
            return;
        }
        send(ex, 405, "text/plain; charset=utf-8", "Method not allowed");
    }

    private static void send(HttpExchange ex, int code, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static final String HTML = """
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="theme-color" content="#f8f8fc">
<meta name="description" content="Daymark daily planner with calendar">
<title>Daymark - Daily Planner</title>
<style>
:root{--ink:#22233b;--muted:#7d8197;--soft:#a1a4b7;--line:#e9eaf2;--panel:#fff;--canvas:#f8f8fc;--purple:#6b5ce7;--purple-dark:#5748d2;--purple-soft:#efedff;--mint:#ddf5eb;--mint-ink:#2b8063;--amber:#fff0d8;--amber-ink:#a86917;--rose:#ffe8eb;--rose-ink:#b84d59;--shadow:0 12px 32px rgba(46,42,89,.045)}
*{box-sizing:border-box}html{min-width:320px;background:var(--canvas)}body{margin:0;color:var(--ink);background:var(--canvas);font:14px/1.5 Inter,ui-sans-serif,system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif}button,input,textarea,select{font:inherit}button{cursor:pointer}button:focus-visible,input:focus-visible,textarea:focus-visible,select:focus-visible{outline:3px solid #6b5ce744;outline-offset:2px}.app-shell{min-height:100vh;display:flex}.sidebar{position:fixed;inset:0 auto 0 0;width:242px;padding:28px 17px 18px;background:#fff;border-right:1px solid var(--line);display:flex;flex-direction:column}.brand{display:flex;align-items:center;gap:10px;margin:0 10px 46px;color:var(--ink);text-decoration:none;width:fit-content}.brand-mark{width:32px;height:32px;border-radius:10px;display:grid;place-items:center;color:#fff;background:var(--purple);font:700 25px/1 Georgia,serif;transform:rotate(-4deg)}.brand-name{font-size:22px;letter-spacing:-1.1px;font-weight:780}.workspace-label{padding:0 12px;margin-bottom:12px;color:#a5a7b7;font-size:10px;font-weight:800;letter-spacing:1.4px}.primary-nav{display:grid;gap:5px}.nav-link{width:100%;border:0;display:flex;align-items:center;gap:12px;text-align:left;padding:11px 12px;color:#777b91;background:transparent;border-radius:10px;font-size:13px;font-weight:570}.nav-link:hover{background:#f8f7ff;color:var(--ink)}.nav-link.active{color:var(--purple-dark);background:var(--purple-soft);font-weight:730}.nav-icon{width:18px;text-align:center;font-size:17px}.nav-count{margin-left:auto;min-width:24px;text-align:center;padding:1px 6px;border-radius:7px;background:#fff;color:#8b83d7;font-size:11px}.sidebar-bottom{margin-top:auto}.sidebar-tip{margin:0 1px 20px;border:1px solid #ebe8ff;background:linear-gradient(145deg,#faf9ff,#f5f3ff);padding:15px 13px 14px;border-radius:13px}.tip-icon{display:inline-grid;place-items:center;width:27px;height:27px;border-radius:8px;color:var(--purple);background:#ece9ff;font-size:15px}.sidebar-tip p{margin:11px 0 0;color:#8888a0;font-size:11px;line-height:1.8}.sidebar-tip strong{color:#41405d;font-size:12px}.profile-row{display:flex;align-items:center;padding:15px 5px 0;border-top:1px solid var(--line);gap:10px}.avatar{width:34px;height:34px;display:grid;place-items:center;border-radius:50%;background:#fce8dc;color:#a65f3d;font-weight:800}.profile-copy{display:grid;min-width:0}.profile-copy strong{color:#414258;font-size:12px}.profile-copy span{color:#a0a2b4;font-size:10px}.profile-dots{margin-left:auto;color:#9294a8;font-size:18px;letter-spacing:1px}.main-column{min-width:0;flex:1;margin-left:242px;display:flex;flex-direction:column}.topbar{padding:34px clamp(24px,4vw,58px) 26px;display:flex;justify-content:space-between;align-items:flex-end;gap:24px;border-bottom:1px solid #eeeff5;background:rgba(255,255,255,.58)}.page-heading{min-width:220px}.eyebrow{margin:0 0 8px;color:#a1a3b4;font-size:10px;font-weight:800;letter-spacing:1.35px}.page-heading h1{margin:0;color:#292a40;font-size:clamp(26px,2.3vw,34px);font-weight:780;letter-spacing:-1.4px;line-height:1.2}.page-subtitle{margin:8px 0 0;color:var(--muted);font-size:12px}.topbar-actions{display:flex;align-items:center;justify-content:flex-end;gap:10px;flex-wrap:wrap}.search-box{min-width:230px;display:flex;align-items:center;gap:8px;padding:0 10px;height:39px;border:1px solid #e7e8f0;border-radius:10px;background:#fff;color:#a0a2b3}.search-box>span{font-size:21px;line-height:1}.search-box input{min-width:0;width:100%;border:0;outline:none;color:var(--ink);background:transparent;font-size:11px}.search-box kbd{white-space:nowrap;border:1px solid #e8e9f0;background:#fafaff;padding:2px 4px;border-radius:4px;color:#a0a2b2;font-size:9px}.button{display:inline-flex;align-items:center;justify-content:center;gap:7px;min-height:39px;padding:0 15px;border:1px solid transparent;border-radius:9px;font-size:11px;font-weight:750;transition:background .15s,transform .15s}.button:active{transform:translateY(1px)}.button-primary{background:var(--purple);color:#fff;box-shadow:0 4px 10px #6b5ce722}.button-primary:hover{background:var(--purple-dark)}.button-secondary{background:#fff;color:#686b82;border-color:var(--line)}.button-secondary:hover{background:#f7f7fc}.button-danger{background:#fff;color:var(--rose-ink);border-color:#f2dfe1}.button-small{min-height:31px;padding:0 10px;font-size:10px}.page-content{width:100%;max-width:1500px;margin:0 auto;padding:27px clamp(24px,4vw,58px) 35px;flex:1}.app-footer{display:flex;justify-content:space-between;padding:14px clamp(24px,4vw,58px) 19px;color:#adafbe;font-size:10px}.app-footer span:first-child{font-weight:800;letter-spacing:-.3px}.section-heading{display:flex;align-items:flex-end;justify-content:space-between;gap:16px;margin-bottom:16px}.section-heading h2{margin:0;font-size:16px;letter-spacing:-.35px}.section-heading p{margin:4px 0 0;color:var(--muted);font-size:11px}.text-link{border:0;padding:5px 0;color:var(--purple-dark);background:transparent;font-size:11px;font-weight:750}.text-link:hover{text-decoration:underline}.stats-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:15px;margin-bottom:27px}.stat-card{min-height:116px;padding:17px 18px;border:1px solid var(--line);border-radius:13px;background:#fff;box-shadow:var(--shadow)}.stat-top{display:flex;justify-content:space-between;align-items:center;gap:10px;color:var(--muted);font-size:11px;font-weight:650}.stat-icon{width:30px;height:30px;border-radius:9px;display:grid;place-items:center;font-size:14px}.stat-icon.purple{color:var(--purple);background:var(--purple-soft)}.stat-icon.mint{color:var(--mint-ink);background:var(--mint)}.stat-icon.amber{color:var(--amber-ink);background:var(--amber)}.stat-value{margin-top:9px;color:#303149;font-size:27px;font-weight:800;letter-spacing:-1px;line-height:1}.stat-footnote{margin-top:7px;color:#a2a4b5;font-size:10px}.dashboard-grid{display:grid;grid-template-columns:minmax(0,1.4fr) minmax(280px,.85fr);gap:18px;align-items:start}.panel{min-width:0;padding:19px;border:1px solid var(--line);border-radius:13px;background:#fff;box-shadow:var(--shadow)}.task-list{display:grid}.task-row{min-width:0;display:flex;align-items:center;gap:11px;padding:13px 0;border-bottom:1px solid #f0f0f6}.task-row:last-child{border-bottom:0;padding-bottom:2px}.check-button{flex:0 0 19px;width:19px;height:19px;border:1.5px solid #d7d8e7;border-radius:6px;background:#fff;color:#fff;display:grid;place-items:center;padding:0;font-size:12px;font-weight:800}.check-button:hover{border-color:var(--purple)}.check-button.done{background:var(--purple);border-color:var(--purple)}.task-main{min-width:0;flex:1}.task-title{overflow-wrap:anywhere;color:#404159;font-size:12px;font-weight:680}.task-title.completed{color:#aaaaba;text-decoration:line-through}.task-description{margin:3px 0 0;color:#a0a2b2;font-size:10px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.task-trailing{display:flex;align-items:center;flex-wrap:wrap;gap:6px;justify-content:flex-end}.pill{display:inline-flex;align-items:center;gap:4px;padding:4px 7px;border-radius:6px;font-size:9px;font-weight:750;white-space:nowrap}.priority-high{background:var(--rose);color:var(--rose-ink)}.priority-medium{background:var(--amber);color:var(--amber-ink)}.priority-low{background:#edf0f8;color:#757a96}.category-pill{background:#f3f3f9;color:#777b91}.due-pill{background:#f3f0ff;color:#7163d7}.due-pill.overdue{background:var(--rose);color:var(--rose-ink)}.two-panels{display:grid;gap:18px}.focus-card{padding:18px;border-radius:12px;background:linear-gradient(140deg,#f6f4ff,#fbfaff);border:1px solid #ece9ff}.focus-eyebrow{color:#8981cb;font-size:10px;font-weight:800;letter-spacing:1px}.focus-card h3{margin:7px 0 5px;font-size:15px;letter-spacing:-.35px}.focus-card p{margin:0;color:#85859c;font-size:11px;line-height:1.7}.progress-track{height:7px;margin:15px 0 7px;overflow:hidden;border-radius:20px;background:#e8e5fc}.progress-fill{height:100%;border-radius:20px;background:var(--purple);transition:width .2s}.progress-meta{display:flex;justify-content:space-between;color:#8989a0;font-size:10px}.mini-stat-row{display:flex;justify-content:space-between;align-items:center;margin-top:13px;padding-top:13px;border-top:1px solid #e9e6fc;color:#777b91;font-size:11px}.mini-stat-row strong{color:#45445f}.week-list{display:grid;gap:10px}.week-item{display:flex;gap:10px;align-items:center}.week-day{width:38px;height:43px;border-radius:9px;background:#f6f6fb;border:1px solid #efeff6;display:grid;place-items:center;align-content:center;line-height:1.15}.week-day strong{font-size:12px;color:#484960}.week-day span{font-size:9px;color:#a0a2b4;text-transform:uppercase;margin-top:4px}.week-copy{flex:1;min-width:0}.week-copy strong{display:block;font-size:11px;color:#4b4c64}.week-copy span{font-size:10px;color:#999caf}.week-num{font-size:10px;color:#888ba0}.notes-preview{margin-top:20px}.note-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:13px}.note-card{padding:15px;border-radius:11px;background:#fff;border:1px solid var(--line);box-shadow:var(--shadow);cursor:pointer;transition:transform .15s,border .15s}.note-card:hover{transform:translateY(-2px);border-color:#dcd7ff}.note-color{width:29px;height:29px;display:grid;place-items:center;border-radius:9px;background:#fff1d9;color:#aa7a28;font-size:14px}.note-card:nth-child(3n+2) .note-color{background:#e5f7f0;color:#3b9275}.note-card:nth-child(3n+3) .note-color{background:#f0edff;color:#796be2}.note-card h3{margin:11px 0 5px;font-size:12px}.note-card p{height:34px;overflow:hidden;margin:0;color:#8b8ea2;font-size:10px;line-height:1.65}.note-meta{display:flex;justify-content:space-between;margin-top:13px;padding-top:10px;border-top:1px solid #f0f0f6;color:#a2a4b4;font-size:9px}.page-toolbar{display:flex;align-items:center;justify-content:space-between;gap:15px;flex-wrap:wrap;margin-bottom:17px}.filter-tabs{display:flex;gap:4px;padding:4px;background:#f0f0f8;border-radius:9px}.filter-tab{border:0;background:transparent;color:#82869a;border-radius:7px;padding:7px 12px;font-size:11px;font-weight:700}.filter-tab.active{background:#fff;color:var(--purple-dark);box-shadow:0 2px 5px #2520440c}.full-task-list{background:#fff;border:1px solid var(--line);border-radius:13px;padding:0 20px;box-shadow:var(--shadow)}.task-date{padding:15px 0 2px;color:#a1a3b4;font-size:10px;font-weight:800;letter-spacing:1px}.empty-state{padding:35px 15px;text-align:center;color:#9699ad;font-size:12px}.notes-layout{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:15px}.notes-layout .note-card{min-height:170px;padding:18px}.notes-layout .note-card p{height:auto;min-height:44px}.note-delete{border:0;background:transparent;color:#aaaabd;padding:2px 5px;font-size:11px}.note-delete:hover{color:var(--rose-ink)}.toast{position:fixed;bottom:22px;right:22px;padding:11px 15px;background:#2d2d47;color:#fff;border-radius:10px;box-shadow:0 8px 28px #22234430;font-size:12px;opacity:0;transform:translateY(8px);pointer-events:none;transition:all .2s;z-index:10}.toast.show{opacity:1;transform:translateY(0)}dialog{width:min(480px,calc(100vw - 30px));padding:0;border:1px solid var(--line);border-radius:16px;box-shadow:0 22px 80px #29244828;color:var(--ink)}dialog::backdrop{background:#22213a55;backdrop-filter:blur(3px)}.modal-head{display:flex;justify-content:space-between;align-items:center;padding:18px 21px;border-bottom:1px solid var(--line)}.modal-head h2{margin:0;font-size:16px}.icon-button{width:30px;height:30px;border:1px solid var(--line);border-radius:8px;color:#7b7e94;background:#fff}.modal-form{padding:20px;display:grid;gap:14px}.field{display:grid;gap:6px}.field label{font-size:11px;font-weight:750;color:#666981}.field input,.field textarea,.field select{width:100%;padding:10px 11px;border:1px solid #e5e6ef;border-radius:9px;background:#fff;color:var(--ink);font-size:12px}.field textarea{min-height:130px;resize:vertical}.form-row{display:grid;grid-template-columns:1fr 1fr;gap:12px}.modal-actions{display:flex;justify-content:flex-end;gap:9px;padding-top:4px}.small-muted{color:#a0a3b4;font-size:10px}.data-badge{display:inline-flex;align-items:center;gap:6px;color:#8e91a6;font-size:10px}.dot{width:6px;height:6px;border-radius:50%;background:#59b78d;display:inline-block}
.cal-toolbar{display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap;margin-bottom:16px}
.cal-nav{display:flex;align-items:center;gap:8px}
.cal-nav h2{margin:0 0 0 4px;font-size:16px;letter-spacing:-.35px;min-width:160px}
.cal-icon-btn{width:32px;height:32px;border:1px solid var(--line);background:#fff;border-radius:8px;color:#6f7288;font-size:15px;line-height:1;display:grid;place-items:center}
.cal-icon-btn:hover{background:#f7f7fc}
.cal-layout{display:grid;grid-template-columns:minmax(0,1fr) 264px;gap:18px;align-items:start}
.cal-wrap{border:1px solid var(--line);border-radius:13px;background:#fff;box-shadow:var(--shadow);overflow:hidden}
.cal-dow-row{display:grid;grid-template-columns:repeat(7,minmax(0,1fr));background:#fbfbfe;border-bottom:1px solid var(--line)}
.cal-dow{padding:9px 4px;text-align:center;font-size:9px;font-weight:800;letter-spacing:.9px;color:#9a9cb0}
.cal-grid{display:grid;grid-template-columns:repeat(7,minmax(0,1fr))}
.cal-cell{min-height:96px;padding:6px;border-right:1px solid #f1f1f7;border-bottom:1px solid #f1f1f7;cursor:pointer;overflow:hidden}
.cal-cell:nth-child(7n){border-right:0}
.cal-cell:hover{background:#fafaff}
.cal-cell.other{background:#fcfcfe}
.cal-cell.other .cal-daynum{color:#c2c4d2}
.cal-daynum{width:23px;height:23px;border-radius:50%;display:grid;place-items:center;font-size:11px;font-weight:730;color:#5a5c74;margin-bottom:2px}
.cal-cell.today .cal-daynum{background:var(--purple);color:#fff}
.cal-chip{margin-top:3px;padding:2px 5px;border-radius:5px;font-size:9px;font-weight:650;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.cal-chip.task{background:#f1f1f8;color:#71758c}
.ev-purple{background:#efedff;color:#5b4ddb}
.ev-mint{background:#ddf5eb;color:#2b8063}
.ev-amber{background:#fff0d8;color:#a86917}
.ev-rose{background:#ffe8eb;color:#b84d59}
.ev-blue{background:#e6eefc;color:#3b6fd4}
.cal-more{margin-top:3px;color:#a0a2b4;font-size:9px;font-weight:700}
.cal-side{display:grid;gap:16px}
.agenda-item{display:flex;gap:9px;padding:9px 0;border-bottom:1px solid #f0f0f6}
.agenda-item:last-child{border-bottom:0;padding-bottom:0}
.agenda-time{font-size:10px;font-weight:750;color:#83869c;min-width:42px;padding-top:1px}
.agenda-copy{min-width:0}
.agenda-copy strong{display:block;font-size:11px;color:#484960;overflow-wrap:anywhere}
.agenda-copy span{font-size:9.5px;color:#9a9db0}
.color-dots{display:flex;gap:7px;padding-top:3px}
.color-dot{width:22px;height:22px;border-radius:50%;border:2px solid transparent;padding:0;cursor:pointer}
.color-dot.sel{border-color:#3a3b52}
.day-list{display:grid;gap:9px;max-height:250px;overflow:auto}
.day-row{display:flex;gap:9px;align-items:flex-start;padding:9px 10px;border-radius:9px;background:#fafafd;border:1px solid #f0f0f6}
@media(max-width:1100px){.sidebar{width:210px}.main-column{margin-left:210px}.topbar{align-items:flex-start;flex-direction:column}.topbar-actions{justify-content:flex-start;width:100%}.dashboard-grid{grid-template-columns:minmax(0,1.2fr) minmax(255px,.9fr)}}
@media(max-width:1180px){.cal-layout{grid-template-columns:1fr}}
@media(max-width:820px){.sidebar{position:static;width:100%;min-height:auto;padding:12px 16px;border-right:0;border-bottom:1px solid var(--line)}.app-shell{display:block}.brand{margin:0 0 12px}.primary-nav{display:flex;gap:4px}.nav-link{width:auto;flex:1;justify-content:center;padding:9px 8px}.nav-count,.sidebar-bottom,.workspace-label{display:none}.main-column{margin:0}.topbar{padding-top:24px}.dashboard-grid{grid-template-columns:1fr}.note-grid,.notes-layout{grid-template-columns:1fr 1fr}.page-content{padding-top:22px}}
@media(max-width:560px){.topbar,.page-content{padding-left:16px;padding-right:16px}.topbar-actions{align-items:stretch}.search-box{min-width:0;flex:1}.stats-grid{gap:8px}.stat-card{padding:13px;min-height:105px}.stat-footnote{font-size:9px}.dashboard-grid{gap:13px}.panel{padding:14px}.task-trailing .category-pill{display:none}.task-row{gap:8px}.note-grid,.notes-layout{grid-template-columns:1fr}.form-row{grid-template-columns:1fr}.app-footer{padding-left:16px;padding-right:16px}.cal-cell{min-height:72px;padding:4px}.cal-chip{font-size:8px}.cal-nav h2{min-width:0;font-size:14px}}
</style>
</head>
<body>
<div class="app-shell">
  <aside class="sidebar">
    <a class="brand" href="#dashboard" data-page="dashboard"><span class="brand-mark">d</span><span class="brand-name">daymark</span></a>
    <div class="workspace-label">WORKSPACE</div>
    <nav class="primary-nav" aria-label="Primary navigation">
      <button class="nav-link active" data-page="dashboard"><span class="nav-icon">&#9707;</span> Dashboard</button>
      <button class="nav-link" data-page="tasks"><span class="nav-icon">&#9745;</span> My tasks <span class="nav-count" id="open-task-count">0</span></button>
      <button class="nav-link" data-page="calendar"><span class="nav-icon">&#9638;</span> Calendar</button>
      <button class="nav-link" data-page="notes"><span class="nav-icon">&#9636;</span> Notes</button>
    </nav>
    <div class="sidebar-bottom">
      <div class="sidebar-tip"><span class="tip-icon">&#10022;</span><p><strong>Make space for focus.</strong><br>One step at a time is still progress.</p></div>
      <div class="profile-row"><div class="avatar">Y</div><div class="profile-copy"><strong>My workspace</strong><span>Personal planner</span></div><span class="profile-dots">&#183;&#183;&#183;</span></div>
    </div>
  </aside>
  <div class="main-column">
    <header class="topbar">
      <div class="page-heading"><p class="eyebrow" id="page-eyebrow">PERSONAL WORKSPACE</p><h1 id="page-title">Your day, in focus.</h1><p class="page-subtitle" id="page-subtitle">A little structure can make room for a lot.</p></div>
      <div class="topbar-actions"><label class="search-box" for="global-search"><span aria-hidden="true">&#8981;</span><input id="global-search" type="search" placeholder="Search tasks and notes..." autocomplete="off"><kbd>&#8984; K</kbd></label><button class="button button-primary" id="new-task-btn"><span>&#65291;</span> New task</button></div>
    </header>
    <main id="page-content" class="page-content" tabindex="-1"></main>
    <footer class="app-footer"><span>daymark</span><span>Plan with intention. Adjust as you go.</span></footer>
  </div>
</div>
<div id="toast" class="toast" role="status" aria-live="polite"></div>

<dialog id="task-dialog"><div class="modal-head"><h2 id="task-dialog-title">Create a task</h2><button class="icon-button" type="button" data-close="task-dialog" aria-label="Close">&#10005;</button></div><form id="task-form" class="modal-form"><div class="field"><label for="task-title-input">Task name</label><input id="task-title-input" name="title" required maxlength="120" placeholder="What do you need to do?"></div><div class="field"><label for="task-description-input">Description (optional)</label><input id="task-description-input" name="description" maxlength="200" placeholder="Add a little context"></div><div class="form-row"><div class="field"><label for="task-category-input">Category</label><select id="task-category-input" name="category"><option>Study</option><option>Project</option><option>Personal</option><option>College</option><option>Work</option></select></div><div class="field"><label for="task-priority-input">Priority</label><select id="task-priority-input" name="priority"><option value="HIGH">High</option><option value="MEDIUM" selected>Medium</option><option value="LOW">Low</option></select></div></div><div class="field"><label for="task-due-input">Due date</label><input id="task-due-input" type="date" name="dueDate"></div><div class="modal-actions"><button class="button button-secondary" type="button" data-close="task-dialog">Cancel</button><button class="button button-primary" type="submit">Save task</button></div></form></dialog>

<dialog id="note-dialog"><div class="modal-head"><h2 id="note-dialog-title">Create a note</h2><button class="icon-button" type="button" data-close="note-dialog" aria-label="Close">&#10005;</button></div><form id="note-form" class="modal-form"><div class="field"><label for="note-title-input">Title</label><input id="note-title-input" name="title" required maxlength="100" placeholder="Give your note a title"></div><div class="field"><label for="note-content-input">Note</label><textarea id="note-content-input" name="content" required maxlength="3000" placeholder="Capture an idea, plan, or thought..."></textarea></div><div class="modal-actions"><button class="button button-secondary" type="button" data-close="note-dialog">Cancel</button><button class="button button-primary" type="submit">Save note</button></div></form></dialog>

<dialog id="event-dialog"><div class="modal-head"><h2 id="event-dialog-title">New event</h2><button class="icon-button" type="button" data-close="event-dialog" aria-label="Close">&#10005;</button></div><form id="event-form" class="modal-form"><div class="field"><label for="event-title-input">Event title</label><input id="event-title-input" name="title" required maxlength="120" placeholder="What is happening?"></div><div class="field"><label for="event-date-input">Date</label><input id="event-date-input" type="date" name="date" required></div><div class="form-row"><div class="field"><label for="event-start-input">Start time</label><input id="event-start-input" type="time" name="start"></div><div class="field"><label for="event-end-input">End time</label><input id="event-end-input" type="time" name="end"></div></div><div class="form-row"><div class="field"><label for="event-recurrence-input">Repeat</label><select id="event-recurrence-input" name="recurrence"><option value="none">Does not repeat</option><option value="daily">Daily</option><option value="weekly">Weekly</option><option value="monthly">Monthly</option></select></div><div class="field"><label>Colour</label><div class="color-dots" id="event-color-picker"></div></div></div><div class="field"><label for="event-description-input">Description (optional)</label><input id="event-description-input" name="description" maxlength="200" placeholder="Add details"></div><div class="modal-actions"><button class="button button-danger" type="button" id="delete-event-btn" style="display:none">Delete</button><button class="button button-secondary" type="button" data-close="event-dialog">Cancel</button><button class="button button-primary" type="submit">Save event</button></div></form></dialog>

<dialog id="day-dialog"><div class="modal-head"><h2 id="day-dialog-title">Day</h2><button class="icon-button" type="button" data-close="day-dialog" aria-label="Close">&#10005;</button></div><div class="modal-form"><div class="day-list" id="day-dialog-list"></div><div class="modal-actions"><button class="button button-secondary" type="button" data-close="day-dialog">Close</button><button class="button button-primary" type="button" id="day-add-event">&#65291; Add event</button></div></div></dialog>

<script>
(() => {
  const $ = (s,root=document) => root.querySelector(s);
  const $$ = (s,root=document) => [...root.querySelectorAll(s)];
  const today = new Date(); today.setHours(0,0,0,0);
  const iso = d => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  const offsetDate = n => { const d = new Date(today); d.setDate(d.getDate()+n); return iso(d); };
  const fmtDate = v => { if(!v) return 'No date'; if(v===iso(today)) return 'Today'; if(v===offsetDate(1)) return 'Tomorrow'; return new Intl.DateTimeFormat(undefined,{month:'short',day:'numeric'}).format(new Date(v+'T00:00:00')); };
  const esc = v => String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[c]));
  const monthStart = d => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-01`;
  const palette = ['purple','mint','amber','rose','blue'];

  const defaultTasks = [
    {id:1,title:'Finish Java planner dashboard',description:'Polish the task overview and test the main interactions.',category:'Project',priority:'HIGH',dueDate:offsetDate(0),completed:false},
    {id:2,title:'Review data structures notes',description:'Revise stacks, queues, and linked lists.',category:'Study',priority:'HIGH',dueDate:offsetDate(0),completed:false},
    {id:3,title:'Practice German vocabulary',description:'Complete a short review session.',category:'Personal',priority:'MEDIUM',dueDate:offsetDate(0),completed:false},
    {id:4,title:'Organize database lab record',description:'Check the remaining exercises and examples.',category:'College',priority:'MEDIUM',dueDate:offsetDate(1),completed:false},
    {id:5,title:'Plan next week',description:'Choose the three most important outcomes.',category:'Personal',priority:'LOW',dueDate:offsetDate(3),completed:true},
    {id:6,title:'Read Spring Boot documentation',description:'Review controllers and request validation.',category:'Study',priority:'LOW',dueDate:offsetDate(2),completed:true}
  ];
  const defaultNotes = [
    {id:1,title:'Project ideas',content:'Keep the first version small. Get tasks, notes, and persistence working before adding advanced features.',updatedAt:'Just now'},
    {id:2,title:'Study plan',content:'Java fundamentals - Spring Boot REST APIs - SQL and data modelling - testing basics.',updatedAt:'Today'},
    {id:3,title:'Weekly reflection',content:'What worked this week? What needs less time? What should be the main focus next week?',updatedAt:'Yesterday'}
  ];
  const defaultEvents = [
    {id:901,title:'Team stand-up',date:offsetDate(0),start:'09:30',end:'09:45',color:'purple',recurrence:'daily',description:'Daily sync with the team'},
    {id:902,title:'Java project review',date:offsetDate(0),start:'15:00',end:'16:00',color:'mint',recurrence:'none',description:'Review the planner build'},
    {id:903,title:'Study block',date:offsetDate(1),start:'18:00',end:'19:30',color:'amber',recurrence:'weekly',description:'Data structures revision'},
    {id:904,title:'Gym session',date:offsetDate(2),start:'07:00',end:'08:00',color:'rose',recurrence:'none',description:''},
    {id:905,title:'Monthly planning',date:offsetDate(5),start:'11:00',end:'12:00',color:'blue',recurrence:'monthly',description:'Review goals for the month'}
  ];

  const getStored = (key,fallback) => { try { const v=localStorage.getItem(key); return v?JSON.parse(v):fallback; } catch { return fallback; } };

  const state = {
    page:'dashboard', filter:'all', search:'',
    tasks:getStored('daymark-demo-tasks',defaultTasks),
    notes:getStored('daymark-demo-notes',defaultNotes),
    events:getStored('daymark-demo-events',defaultEvents),
    editingTask:null, editingNote:null, editingEvent:null,
    eventColor:'purple', calCursor:monthStart(today), dayDialogDate:null
  };

  const pageInfo={
    dashboard:{title:'Your day, in focus.',subtitle:'A little structure can make room for a lot.'},
    tasks:{title:'My tasks',subtitle:'Keep the important things moving.'},
    calendar:{title:'Your calendar',subtitle:'Every plan, in one place.'},
    notes:{title:'Notes and ideas',subtitle:'A place for thoughts worth keeping.'}
  };

  let saveTimer=null;
  function persist(){
    try{
      localStorage.setItem('daymark-demo-tasks',JSON.stringify(state.tasks));
      localStorage.setItem('daymark-demo-notes',JSON.stringify(state.notes));
      localStorage.setItem('daymark-demo-events',JSON.stringify(state.events));
    }catch(e){ }
    clearTimeout(saveTimer);
    saveTimer=setTimeout(()=>{
      fetch('/api/data',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({tasks:state.tasks,notes:state.notes,events:state.events})}).catch(()=>{});
    },350);
  }

  function toast(msg){const t=$('#toast');t.textContent=msg;t.classList.add('show');clearTimeout(toast.timer);toast.timer=setTimeout(()=>t.classList.remove('show'),2200);}
  function priorityPill(p){return `<span class="pill priority-${String(p||'MEDIUM').toLowerCase()}">${esc((p||'MEDIUM').toLowerCase())}</span>`;}

  function taskRow(t){
    return `<div class="task-row"><button class="check-button ${t.completed?'done':''}" data-toggle-task="${t.id}" aria-label="${t.completed?'Mark incomplete':'Mark complete'}">${t.completed?'&#10003;':''}</button><div class="task-main"><div class="task-title ${t.completed?'completed':''}">${esc(t.title)}</div><p class="task-description">${esc(t.description||t.category||'')}</p></div><div class="task-trailing"><span class="pill category-pill">${esc(t.category||'Other')}</span>${priorityPill(t.priority)}<span class="pill ${t.dueDate&&t.dueDate<iso(today)&&!t.completed?'due-pill overdue':'due-pill'}">${esc(fmtDate(t.dueDate))}</span></div></div>`;
  }
  function noteCard(n){
    return `<article class="note-card" data-open-note="${n.id}" tabindex="0" role="button" aria-label="Edit note ${esc(n.title)}"><div class="note-color">&#9998;</div><h3>${esc(n.title)}</h3><p>${esc(n.content)}</p><div class="note-meta"><span>${esc(n.updatedAt||'Recently updated')}</span><button class="note-delete" type="button" data-delete-note="${n.id}" aria-label="Delete note">Delete</button></div></article>`;
  }

  /* ---------- calendar helpers ---------- */
  function occursOn(ev,d){
    if(!ev.date || d<ev.date) return false;
    const a=new Date(ev.date+'T00:00:00');
    const b=new Date(d+'T00:00:00');
    const days=Math.round((b-a)/86400000);
    const rec=ev.recurrence||'none';
    if(rec==='daily') return true;
    if(rec==='weekly') return days%7===0;
    if(rec==='monthly') return a.getDate()===b.getDate();
    return days===0;
  }
  function eventsOn(d){
    return state.events.filter(ev=>occursOn(ev,d)).sort((a,b)=>(a.start||'00:00').localeCompare(b.start||'00:00'));
  }
  function upcomingEvents(limit){
    const out=[];
    for(let i=0;i<60 && out.length<limit;i++){
      const ds=offsetDate(i);
      eventsOn(ds).forEach(ev=>{
        if(out.length<limit) out.push({title:ev.title,time:ev.start||'All day',when:i===0?'Today':i===1?'Tomorrow':fmtDate(ds)});
      });
    }
    return out;
  }

  function filteredTasks(){
    return state.tasks.filter(t=>(state.filter==='all'||(state.filter==='open'&&!t.completed)||(state.filter==='completed'&&t.completed))&&(!state.search||`${t.title} ${t.description} ${t.category}`.toLowerCase().includes(state.search.toLowerCase())));
  }

  function renderDashboard(){
    const total=state.tasks.length,completed=state.tasks.filter(t=>t.completed).length,open=total-completed;
    const todayOpen=state.tasks.filter(t=>t.dueDate===iso(today)&&!t.completed).length;
    const rate=total?Math.round(completed/total*100):0;
    const tasks=state.tasks.filter(t=>!t.completed&&(!state.search||`${t.title} ${t.description} ${t.category}`.toLowerCase().includes(state.search.toLowerCase()))).slice(0,4);
    const notes=state.notes.filter(n=>!state.search||`${n.title} ${n.content}`.toLowerCase().includes(state.search.toLowerCase())).slice(0,3);
    const todays=eventsOn(iso(today));
    return `<div class="stats-grid">
      <section class="stat-card"><div class="stat-top"><span>Open tasks</span><span class="stat-icon purple">&#9783;</span></div><div class="stat-value">${open}</div><div class="stat-footnote">Tasks still on your list</div></section>
      <section class="stat-card"><div class="stat-top"><span>Completed</span><span class="stat-icon mint">&#10003;</span></div><div class="stat-value">${completed}</div><div class="stat-footnote">Small wins add up</div></section>
      <section class="stat-card"><div class="stat-top"><span>Due today</span><span class="stat-icon amber">&#9719;</span></div><div class="stat-value">${todayOpen}</div><div class="stat-footnote">Keep today's priorities clear</div></section>
    </div>
    <div class="dashboard-grid">
      <section class="panel">
        <div class="section-heading"><div><h2>Today's focus</h2><p>Start with what matters most.</p></div><button class="text-link" data-page="tasks">View all tasks &#8594;</button></div>
        <div class="task-list">${tasks.length?tasks.map(taskRow).join(''):'<div class="empty-state">No matching open tasks. Enjoy the breathing room.</div>'}</div>
        <div class="notes-preview"><div class="section-heading"><div><h2>Recent notes</h2><p>Thoughts worth keeping close.</p></div><button class="text-link" data-page="notes">All notes &#8594;</button></div>
        <div class="note-grid">${notes.length?notes.map(noteCard).join(''):'<div class="empty-state">No matching notes.</div>'}</div></div>
      </section>
      <div class="two-panels">
        <section class="panel"><div class="section-heading"><div><h2>Today's schedule</h2><p>${todays.length} ${todays.length===1?'event':'events'} today.</p></div><button class="text-link" data-page="calendar">Calendar &#8594;</button></div>
          <div>${todays.length?todays.map(ev=>`<div class="agenda-item"><div class="agenda-time">${esc(ev.start||'All day')}</div><div class="agenda-copy"><strong>${esc(ev.title)}</strong><span>${esc(ev.start&&ev.end?ev.start+' - '+ev.end:'No time set')}</span></div></div>`).join(''):'<div class="empty-state">Nothing scheduled today.</div>'}</div>
        </section>
        <section class="panel"><div class="section-heading"><div><h2>Your progress</h2><p>A quick look at how things are moving.</p></div></div>
          <div class="focus-card"><div class="focus-eyebrow">TASK COMPLETION</div><h3>${rate}% of tasks completed</h3><p>Progress is a useful signal, not a reason to overload your day.</p><div class="progress-track"><div class="progress-fill" style="width:${rate}%"></div></div><div class="progress-meta"><span>${completed} completed</span><span>${total} total</span></div><div class="mini-stat-row"><span>Due today</span><strong>${todayOpen} ${todayOpen===1?'task':'tasks'}</strong></div><div class="mini-stat-row"><span>Still open</span><strong>${open} ${open===1?'task':'tasks'}</strong></div></div>
        </section>
        <section class="panel"><div class="section-heading"><div><h2>Coming up</h2><p>Next dates on your planner.</p></div></div>
          <div class="week-list">${state.tasks.filter(t=>!t.completed&&t.dueDate).sort((a,b)=>a.dueDate.localeCompare(b.dueDate)).slice(0,4).map(t=>`<div class="week-item"><div class="week-day"><strong>${new Date(t.dueDate+'T00:00:00').getDate()}</strong><span>${new Intl.DateTimeFormat(undefined,{month:'short'}).format(new Date(t.dueDate+'T00:00:00'))}</span></div><div class="week-copy"><strong>${esc(t.title)}</strong><span>${esc(t.category||'Task')}</span></div><span class="week-num">${esc(fmtDate(t.dueDate))}</span></div>`).join('')||'<div class="empty-state">Nothing scheduled yet.</div>'}</div>
        </section>
        <div class="data-badge"><span class="dot"></span> Saved by the Java server</div>
      </div>
    </div>`;
  }

  function renderTasks(){
    const ts=filteredTasks();
    return `<div class="page-toolbar"><div class="filter-tabs" role="group" aria-label="Task filter"><button class="filter-tab ${state.filter==='all'?'active':''}" data-filter="all">All tasks</button><button class="filter-tab ${state.filter==='open'?'active':''}" data-filter="open">To do</button><button class="filter-tab ${state.filter==='completed'?'active':''}" data-filter="completed">Completed</button></div><button class="button button-secondary" id="clear-completed">Clear completed</button></div><section class="full-task-list"><div class="task-date">${state.filter==='completed'?'COMPLETED TASKS':state.filter==='open'?'TASKS TO DO':'YOUR TASK LIST'} &#183; ${ts.length}</div><div class="task-list">${ts.length?ts.map(taskRow).join(''):'<div class="empty-state">No tasks match this view.</div>'}</div></section>`;
  }

  function renderNotes(){
    const ns=state.notes.filter(n=>!state.search||`${n.title} ${n.content}`.toLowerCase().includes(state.search.toLowerCase()));
    return `<div class="page-toolbar"><div><span class="small-muted">${ns.length} notes in your workspace</span></div><button class="button button-primary" id="new-note-btn">&#65291; New note</button></div><div class="notes-layout">${ns.length?ns.map(noteCard).join(''):'<div class="empty-state">No notes found. Create a note to capture an idea.</div>'}</div>`;
  }

  function renderCalendar(){
    const cursor = new Date(state.calCursor+'T00:00:00');
    const year = cursor.getFullYear();
    const month = cursor.getMonth();
    const firstDay = new Date(year, month, 1);
    const startDay = firstDay.getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const prevMonthDays = new Date(year, month, 0).getDate();
    
    let cells = [];
    for (let i = startDay - 1; i >= 0; i--) {
        const d = prevMonthDays - i;
        const dateStr = iso(new Date(year, month - 1, d));
        cells.push({ day: d, date: dateStr, other: true });
    }
    for (let i = 1; i <= daysInMonth; i++) {
        const dateStr = iso(new Date(year, month, i));
        cells.push({ day: i, date: dateStr, other: false });
    }
    const remaining = 42 - cells.length;
    for (let i = 1; i <= remaining; i++) {
        const dateStr = iso(new Date(year, month + 1, i));
        cells.push({ day: i, date: dateStr, other: true });
    }
    
    const monthName = cursor.toLocaleString(undefined, { month: 'long', year: 'numeric' });
    
    return `
    <div class="cal-toolbar">
        <div class="cal-nav">
            <button class="cal-icon-btn" id="cal-prev">&#8249;</button>
            <button class="cal-icon-btn" id="cal-next">&#8250;</button>
            <h2>${monthName}</h2>
        </div>
        <div class="cal-nav">
            <button class="button button-secondary button-small" id="cal-today">Today</button>
            <button class="button button-primary button-small" id="cal-add-event">&#65291; New event</button>
        </div>
    </div>
    <div class="cal-layout">
        <div class="cal-wrap">
            <div class="cal-dow-row">
                <div class="cal-dow">SUN</div><div class="cal-dow">MON</div><div class="cal-dow">TUE</div>
                <div class="cal-dow">WED</div><div class="cal-dow">THU</div><div class="cal-dow">FRI</div><div class="cal-dow">SAT</div>
            </div>
            <div class="cal-grid">
                ${cells.map(c => {
                    const evs = eventsOn(c.date);
                    const isToday = c.date === iso(today);
                    return `<div class="cal-cell ${c.other ? 'other' : ''} ${isToday ? 'today' : ''}" data-day="${c.date}">
                        <div class="cal-daynum">${c.day}</div>
                        ${evs.slice(0, 3).map(ev => `<div class="cal-chip ev-${ev.color || 'purple'}">${esc(ev.title)}</div>`).join('')}
                        ${evs.length > 3 ? `<div class="cal-more">+${evs.length - 3} more</div>` : ''}
                    </div>`;
                }).join('')}
            </div>
        </div>
        <div class="cal-side">
            <div class="panel">
                <div class="section-heading"><div><h2>Upcoming</h2><p>Next 5 events.</p></div></div>
                <div>
                    ${upcomingEvents(5).map(ev => `<div class="agenda-item"><div class="agenda-time">${esc(ev.time)}</div><div class="agenda-copy"><strong>${esc(ev.title)}</strong><span>${esc(ev.when)}</span></div></div>`).join('') || '<div class="empty-state">No upcoming events.</div>'}
                </div>
            </div>
        </div>
    </div>`;
  }

  function renderColorPicker() {
      const colors = {
          purple: 'var(--purple-soft)',
          mint: 'var(--mint)',
          amber: 'var(--amber)',
          rose: 'var(--rose)',
          blue: '#e6eefc'
      };
      const picker = $('#event-color-picker');
      picker.innerHTML = palette.map(color => 
          `<button type="button" class="color-dot ${state.eventColor === color ? 'sel' : ''}" data-color="${color}" style="background:${colors[color]};width:22px;height:22px;border-radius:50%;border:2px solid ${state.eventColor === color ? '#3a3b52' : 'transparent'};cursor:pointer;"></button>`
      ).join('');
  }

  function render(){
    const info=pageInfo[state.page];
    $('#page-title').textContent=info.title;
    $('#page-subtitle').textContent=info.subtitle;
    $('#page-eyebrow').textContent=state.page==='dashboard'?'PERSONAL WORKSPACE':state.page.toUpperCase();
    $$('.nav-link').forEach(b=>b.classList.toggle('active',b.dataset.page===state.page));
    $('#open-task-count').textContent=state.tasks.filter(t=>!t.completed).length;
    $('#new-task-btn').style.display=state.page==='notes'||state.page==='calendar'?'none':'inline-flex';
    $('#page-content').innerHTML=state.page==='tasks'?renderTasks():state.page==='notes'?renderNotes():state.page==='calendar'?renderCalendar():renderDashboard();
  }

  function openTask(task=null){
    state.editingTask=task?task.id:null;
    const f=$('#task-form');f.reset();
    $('#task-dialog-title').textContent=task?'Edit task':'Create a task';
    $('#task-title-input').value=task?.title||'';
    $('#task-description-input').value=task?.description||'';
    $('#task-category-input').value=task?.category||'Study';
    $('#task-priority-input').value=task?.priority||'MEDIUM';
    $('#task-due-input').value=task?.dueDate||iso(today);
    $('#task-dialog').showModal();
    setTimeout(()=>$('#task-title-input').focus(),40);
  }

  function openNote(note=null){
    state.editingNote=note?note.id:null;
    $('#note-form').reset();
    $('#note-dialog-title').textContent=note?'Edit note':'Create a note';
    $('#note-title-input').value=note?.title||'';
    $('#note-content-input').value=note?.content||'';
    $('#note-dialog').showModal();
    setTimeout(()=>$('#note-title-input').focus(),40);
  }

  function openEvent(ev=null, defaultDate=null) {
    state.editingEvent = ev ? ev.id : null;
    const f = $('#event-form');
    f.reset();
    $('#event-dialog-title').textContent = ev ? 'Edit event' : 'New event';
    $('#event-title-input').value = ev ? ev.title : '';
    $('#event-date-input').value = ev ? ev.date : (defaultDate || iso(today));
    $('#event-start-input').value = ev ? ev.start : '09:00';
    $('#event-end-input').value = ev ? ev.end : '10:00';
    $('#event-recurrence-input').value = ev ? ev.recurrence : 'none';
    $('#event-description-input').value = ev ? ev.description : '';
    state.eventColor = ev ? ev.color : 'purple';
    renderColorPicker();
    $('#delete-event-btn').style.display = ev ? 'inline-flex' : 'none';
    $('#event-dialog').showModal();
    setTimeout(() => $('#event-title-input').focus(), 40);
  }

  function openDayDialog(dateStr) {
      const evs = eventsOn(dateStr);
      const title = new Date(dateStr + 'T00:00:00').toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' });
      $('#day-dialog-title').textContent = title;
      $('#day-dialog-list').innerHTML = evs.length 
          ? evs.map(ev => `<div class="day-row" data-edit-event="${ev.id}"><div class="agenda-time">${esc(ev.start || 'All day')}</div><div class="agenda-copy"><strong>${esc(ev.title)}</strong><span>${esc(ev.start && ev.end ? ev.start + ' - ' + ev.end : '')}</span></div></div>`).join('')
          : '<div class="empty-state">No events on this day.</div>';
      $('#day-dialog').showModal();
  }

  document.addEventListener('click',e=>{
    const page=e.target.closest('[data-page]');
    if(page){e.preventDefault();state.page=page.dataset.page;render();return;}
    
    const toggle=e.target.closest('[data-toggle-task]');
    if(toggle){
        const t=state.tasks.find(x=>x.id===Number(toggle.dataset.toggleTask));
        if(t){t.completed=!t.completed;persist();render();toast(t.completed?'Task completed':'Task reopened');}
        return;
    }
    
    const filter=e.target.closest('[data-filter]');
    if(filter){state.filter=filter.dataset.filter;render();return;}
    
    const close=e.target.closest('[data-close]');
    if(close){$('#'+close.dataset.close).close();return;}
    
    const openNoteBtn=e.target.closest('[data-open-note]');
    if(openNoteBtn&&!e.target.closest('[data-delete-note]')){
        const n=state.notes.find(x=>x.id===Number(openNoteBtn.dataset.openNote));
        if(n)openNote(n);
        return;
    }
    
    const del=e.target.closest('[data-delete-note]');
    if(del){
        e.stopPropagation();
        const n=state.notes.find(x=>x.id===Number(del.dataset.deleteNote));
        if(n&&confirm(`Delete "${n.title}"?`)){
            state.notes=state.notes.filter(x=>x.id!==n.id);
            persist();
            render();
            toast('Note deleted');
        }
        return;
    }
    
    if(e.target.closest('#new-note-btn')){openNote();return;}
    
    if(e.target.closest('#clear-completed')){
        const before=state.tasks.length;
        state.tasks=state.tasks.filter(t=>!t.completed);
        persist();
        render();
        toast(`${before-state.tasks.length} completed tasks removed`);
    }

    if (e.target.closest('#cal-prev')) {
        const c = new Date(state.calCursor + 'T00:00:00');
        c.setMonth(c.getMonth() - 1);
        state.calCursor = monthStart(c);
        render();
        return;
    }
    if (e.target.closest('#cal-next')) {
        const c = new Date(state.calCursor + 'T00:00:00');
        c.setMonth(c.getMonth() + 1);
        state.calCursor = monthStart(c);
        render();
        return;
    }
    if (e.target.closest('#cal-today')) {
        state.calCursor = monthStart(today);
        render();
        return;
    }
    if (e.target.closest('#cal-add-event')) {
        openEvent(null, iso(today));
        return;
    }
    if (e.target.closest('.cal-cell')) {
        const date = e.target.closest('.cal-cell').dataset.day;
        state.dayDialogDate = date;
        openDayDialog(date);
        return;
    }
    if (e.target.closest('#day-add-event')) {
        $('#day-dialog').close();
        openEvent(null, state.dayDialogDate);
        return;
    }
    if (e.target.closest('[data-color]')) {
        state.eventColor = e.target.closest('[data-color]').dataset.color;
        renderColorPicker();
        return;
    }
    const editEvBtn = e.target.closest('[data-edit-event]');
    if (editEvBtn) {
        const ev = state.events.find(x => x.id === Number(editEvBtn.dataset.editEvent));
        if (ev) {
            $('#day-dialog').close();
            openEvent(ev);
        }
        return;
    }
  });

  $('#new-task-btn').addEventListener('click',()=>openTask());

  $('#task-form').addEventListener('submit',e=>{
    e.preventDefault();
    const fd=new FormData(e.currentTarget);
    const data={title:fd.get('title').trim(),description:fd.get('description').trim(),category:fd.get('category'),priority:fd.get('priority'),dueDate:fd.get('dueDate')||null};
    if(!data.title)return;
    if(state.editingTask!==null){
        const t=state.tasks.find(x=>x.id===state.editingTask);
        Object.assign(t,data);
    }else{
        state.tasks.unshift({id:Date.now(),...data,completed:false});
    }
    persist();
    $('#task-dialog').close();
    render();
    toast(state.editingTask!==null?'Task updated':'Task created');
    state.editingTask=null;
  });

  $('#note-form').addEventListener('submit',e=>{
    e.preventDefault();
    const fd=new FormData(e.currentTarget);
    const data={title:fd.get('title').trim(),content:fd.get('content').trim(),updatedAt:'Just now'};
    if(!data.title||!data.content)return;
    if(state.editingNote!==null){
        Object.assign(state.notes.find(n=>n.id===state.editingNote),data);
    }else{
        state.notes.unshift({id:Date.now(),...data});
    }
    persist();
    $('#note-dialog').close();
    state.page='notes';
    render();
    toast(state.editingNote!==null?'Note updated':'Note created');
    state.editingNote=null;
  });

  $('#event-form').addEventListener('submit', e => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const data = {
        title: fd.get('title').trim(),
        date: fd.get('date'),
        start: fd.get('start'),
        end: fd.get('end'),
        recurrence: fd.get('recurrence'),
        description: fd.get('description').trim(),
        color: state.eventColor
    };
    if (!data.title) return;
    if (state.editingEvent !== null) {
        const ev = state.events.find(x => x.id === state.editingEvent);
        Object.assign(ev, data);
    } else {
        state.events.push({ id: Date.now(), ...data });
    }
    persist();
    $('#event-dialog').close();
    render();
    toast(state.editingEvent !== null ? 'Event updated' : 'Event created');
    state.editingEvent = null;
  });

  $('#delete-event-btn').addEventListener('click', () => {
      if (state.editingEvent !== null) {
          state.events = state.events.filter(x => x.id !== state.editingEvent);
          persist();
          $('#event-dialog').close();
          render();
          toast('Event deleted');
          state.editingEvent = null;
      }
  });

  $('#global-search').addEventListener('input',e=>{state.search=e.target.value;render();});
  
  document.addEventListener('keydown',e=>{
    if((e.metaKey||e.ctrlKey)&&e.key.toLowerCase()==='k'){e.preventDefault();$('#global-search').focus();}
    if(e.key==='Escape'){$$('dialog[open]').forEach(d=>d.close());}
    if((e.key==='Enter'||e.key===' ')&&e.target.matches('[data-open-note]')&&!e.target.closest('[data-delete-note]')){
        e.preventDefault();
        const n=state.notes.find(x=>x.id===Number(e.target.dataset.openNote));
        if(n)openNote(n);
    }
  });
  
  render();
})();
</script>
</body>
</html>
""";
}