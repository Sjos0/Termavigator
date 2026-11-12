// Testa os scripts do modo desenvolvedor (os mesmos strings de DomScripts.kt) contra um site fictício.
import { readFileSync } from "node:fs";
import { JSDOM } from "jsdom";

const kt = readFileSync("app/src/main/java/com/example/devmode/DomScripts.kt", "utf8");
function raw(name) {
  const m = kt.match(new RegExp('val ' + name + ' = """([\\s\\S]*?)"""'));
  if (!m) throw new Error("script não encontrado: " + name);
  return m[1];
}
const EXTRACT = raw("EXTRACT");
const PICKER = raw("PICKER_INSTALL");
const SEND = raw("SEND_BODY");
const html = readFileSync("tests/site-ficticio/fixture.html", "utf8");
const wait = (ms) => new Promise((r) => setTimeout(r, ms));

function novaPagina() {
  const events = [];
  const picks = [];
  const dom = new JSDOM(html, { runScripts: "dangerously", pretendToBeVisual: true });
  const w = dom.window;
  w.TermavigatorDev = {
    onCycle: (j) => events.push(JSON.parse(j)),
    picked: (j) => picks.push(JSON.parse(j)),
  };
  return { w, events, picks };
}

let fails = 0;
function check(nome, cond, extra = "") {
  console.log((cond ? "PASS " : "FAIL ") + nome + (extra ? " — " + extra : ""));
  if (!cond) fails++;
}

{
  const { w } = novaPagina();
  const json = JSON.parse(w.eval(EXTRACT));
  const tags = json.elements.map((e) => e.tag);
  check("extrai campo e botão da página", tags.includes("textarea") && tags.includes("button"), JSON.stringify(tags));
}

{
  const { w, picks } = novaPagina();
  w.eval(PICKER);
  w.eval("window.__tvPickMode = true;");
  w.document.getElementById("enviar").click();
  check("seletor por toque devolve o id do botão", picks.length === 1 && picks[0].selector === "#enviar", JSON.stringify(picks));
}

{
  const { w, events } = novaPagina();
  const cfg = { input: "textarea#prompt", send: "#enviar", thinking: ".pensando", response: ".resposta", stop: "#parar", prompt: "Olá, teste", timeoutMs: 60000 };
  w.eval("(function (cfg) {" + SEND + "})(" + JSON.stringify(cfg) + ");");
  await wait(12000);
  const done = events.find((e) => e.state === "done");
  check("envia o prompt ao campo", w.document.querySelector("textarea#prompt").value === "Olá, teste");
  check("passa por enviando e termina em done com a resposta", !!done && done.text.includes("Resposta da IA"), JSON.stringify(events.map((e) => e.state)));
}

{
  const { w, events } = novaPagina();
  const cfg = { input: "textarea#prompt", send: "#nao-existe", thinking: ".pensando", response: ".resposta", stop: "#parar", prompt: "x", timeoutMs: 5000 };
  w.eval("(function (cfg) {" + SEND + "})(" + JSON.stringify(cfg) + ");");
  await wait(1000);
  check("botão inexistente vira erro, sem travar", events.some((e) => e.state === "error"), JSON.stringify(events));
}

console.log(fails === 0 ? "TODOS OS TESTES PASSARAM" : fails + " FALHA(S)");
process.exit(fails === 0 ? 0 : 1);
