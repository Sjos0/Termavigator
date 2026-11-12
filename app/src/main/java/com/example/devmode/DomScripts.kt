package com.example.devmode

import org.json.JSONObject

/** Scripts injetados na página durante a calibração. Não guardam nada fora da página. */
object DomScripts {

    /** Árvore reduzida: só os elementos úteis para calibrar a interface. */
    const val EXTRACT = """
(function () {
  var nodes = document.querySelectorAll('textarea, input, button, [contenteditable="true"], [role="button"], [aria-label], [data-testid], [class*="message"], [class*="loading"], [class*="typing"]');
  var out = [];
  nodes.forEach(function (el, i) {
    if (out.length >= 300) return;
    var r = el.getBoundingClientRect();
    out.push({
      i: i,
      tag: el.tagName.toLowerCase(),
      id: el.id || '',
      cls: String(el.className || '').slice(0, 120),
      aria: el.getAttribute('aria-label') || '',
      placeholder: el.getAttribute('placeholder') || '',
      testid: el.getAttribute('data-testid') || '',
      text: (el.innerText || '').trim().slice(0, 80),
      visible: r.width > 0 && r.height > 0
    });
  });
  return JSON.stringify({ title: document.title, url: location.href, count: out.length, elements: out });
})();
"""

    /** Instala o seletor de elementos (idempotente). Só age quando window.__tvPickMode estiver ligado. */
    const val PICKER_INSTALL = """
(function () {
  if (window.__tvPickerInstalled) return 'ok';
  window.__tvPickerInstalled = true;

  function clean(s) { return String(s || '').replace(/["']/g, ''); }

  function selectorFor(el) {
    if (el.id && /^[A-Za-z][\w-]*$/.test(el.id)) return '#' + el.id;
    var testid = el.getAttribute('data-testid');
    if (testid) return '[data-testid="' + clean(testid) + '"]';
    var aria = el.getAttribute('aria-label');
    if (aria) return el.tagName.toLowerCase() + '[aria-label="' + clean(aria) + '"]';
    var parts = [];
    var node = el;
    while (node && node.nodeType === 1 && node !== document.body && parts.length < 5) {
      var part = node.tagName.toLowerCase();
      var cls = Array.prototype.slice.call(node.classList || [])
        .filter(function (c) { return /^[A-Za-z_-][\w-]*$/.test(c); })
        .slice(0, 2);
      if (cls.length) part += '.' + cls.join('.');
      var parent = node.parentElement;
      if (parent) {
        var sameTag = Array.prototype.filter.call(parent.children, function (c) { return c.tagName === node.tagName; });
        if (sameTag.length > 1) part += ':nth-of-type(' + (sameTag.indexOf(node) + 1) + ')';
      }
      parts.unshift(part);
      node = parent;
    }
    return parts.join(' > ');
  }

  document.addEventListener('click', function (e) {
    if (!window.__tvPickMode) return;
    e.preventDefault();
    e.stopPropagation();
    window.__tvPickMode = false;
    var el = e.target;
    window.TermavigatorDev.picked(JSON.stringify({
      selector: selectorFor(el),
      tag: el.tagName.toLowerCase(),
      aria: el.getAttribute('aria-label') || '',
      text: (el.innerText || '').trim().slice(0, 80)
    }));
  }, true);
  return 'ok';
})();
"""

    const val PICK_ON = "window.__tvPickMode = true; 'ok';"

    /** Quantos elementos o seletor encontra na página. Retorna -1 se o seletor for inválido. */
    fun countScript(selector: String): String =
        "(function (s) { try { return document.querySelectorAll(s).length; } catch (e) { return -1; } })(" +
            JSONObject.quote(selector) + ");"
}
