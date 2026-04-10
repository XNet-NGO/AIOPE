package com.aiope2.feature.chat

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LatexPdfExporterTest {

  private fun transpile(latex: String): String = LatexPdfExporter.transpileToHtml(latex)

  @Test
  fun `transpileToHtml wraps content in paragraph tags`() {
    val result = transpile("Hello world")
    assertTrue(result.startsWith("<p>"))
    assertTrue(result.endsWith("</p>"))
  }

  @Test
  fun `transpileToHtml handles empty input`() {
    val result = transpile("")
    assertTrue(result.startsWith("<p>"))
    assertTrue(result.endsWith("</p>"))
  }

  @Test
  fun `transpileToHtml converts chapter to h1`() {
    val result = transpile("""\chapter{Introduction}""")
    assertTrue(result.contains("<h1>Introduction</h1>"))
  }

  @Test
  fun `transpileToHtml converts section to h2`() {
    val result = transpile("""\section{Background}""")
    assertTrue(result.contains("<h2>Background</h2>"))
  }

  @Test
  fun `transpileToHtml converts subsection to h3`() {
    val result = transpile("""\subsection{Details}""")
    assertTrue(result.contains("<h3>Details</h3>"))
  }

  @Test
  fun `transpileToHtml converts subsubsection to h4`() {
    val result = transpile("""\subsubsection{Sub-details}""")
    assertTrue(result.contains("<h4>Sub-details</h4>"))
  }

  @Test
  fun `transpileToHtml converts textbf to bold`() {
    val result = transpile("""\textbf{important}""")
    assertTrue(result.contains("<b>important</b>"))
  }

  @Test
  fun `transpileToHtml converts textit to italic`() {
    val result = transpile("""\textit{emphasized}""")
    assertTrue(result.contains("<i>emphasized</i>"))
  }

  @Test
  fun `transpileToHtml converts emph to italic`() {
    val result = transpile("""\emph{stressed}""")
    assertTrue(result.contains("<i>stressed</i>"))
  }

  @Test
  fun `transpileToHtml converts texttt to code`() {
    val result = transpile("""\texttt{inline_code}""")
    assertTrue(result.contains("<code>inline_code</code>"))
  }

  @Test
  fun `transpileToHtml converts underline`() {
    val result = transpile("""\underline{underlined text}""")
    assertTrue(result.contains("<u>underlined text</u>"))
  }

  @Test
  fun `transpileToHtml converts footnote to superscript`() {
    val result = transpile("""text\footnote{note here}""")
    assertTrue(result.contains("<sup>"))
    assertTrue(result.contains("note here"))
  }

  @Test
  fun `transpileToHtml converts itemize environment`() {
    val result = transpile("""\begin{itemize}\item first\item second\end{itemize}""")
    assertTrue(result.contains("<ul>"))
    assertTrue(result.contains("</ul>"))
    assertTrue(result.contains("<li>"))
  }

  @Test
  fun `transpileToHtml converts enumerate environment`() {
    val result = transpile("""\begin{enumerate}\item one\item two\end{enumerate}""")
    assertTrue(result.contains("<ol>"))
    assertTrue(result.contains("</ol>"))
    assertTrue(result.contains("<li>"))
  }

  @Test
  fun `transpileToHtml converts verbatim environment`() {
    val result = transpile("""\begin{verbatim}code block\end{verbatim}""")
    assertTrue(result.contains("<pre>"))
    assertTrue(result.contains("</pre>"))
  }

  @Test
  fun `transpileToHtml converts lstlisting environment`() {
    val result = transpile("""\begin{lstlisting}[language=Python]print("hi")\end{lstlisting}""")
    assertTrue(result.contains("<pre>"))
    assertTrue(result.contains("</pre>"))
  }

  @Test
  fun `transpileToHtml converts quote environment to blockquote`() {
    val result = transpile("""\begin{quote}Quoted text\end{quote}""")
    assertTrue(result.contains("<blockquote>"))
    assertTrue(result.contains("</blockquote>"))
  }

  @Test
  fun `transpileToHtml converts abstract environment`() {
    val result = transpile("""\begin{abstract}Summary here\end{abstract}""")
    assertTrue(result.contains("<blockquote>"))
    assertTrue(result.contains("Abstract"))
    assertTrue(result.contains("Summary here"))
  }

  @Test
  fun `transpileToHtml converts title`() {
    val result = transpile("""\title{My Document}""")
    assertTrue(result.contains("<h1>My Document</h1>"))
  }

  @Test
  fun `transpileToHtml converts author`() {
    val result = transpile("""\author{Jane Doe}""")
    assertTrue(result.contains("<i>Jane Doe</i>"))
  }

  @Test
  fun `transpileToHtml converts date`() {
    val result = transpile("""\date{2024-01-01}""")
    assertTrue(result.contains("<i>2024-01-01</i>"))
  }

  @Test
  fun `transpileToHtml converts href`() {
    val result = transpile("""\href{https://example.com}{Example Link}""")
    assertTrue(result.contains("""<a href="https://example.com">Example Link</a>"""))
  }

  @Test
  fun `transpileToHtml converts url`() {
    val result = transpile("""\url{https://example.com}""")
    assertTrue(result.contains("""href="https://example.com""""))
  }

  @Test
  fun `transpileToHtml converts double backslash to br`() {
    val result = transpile("""line1\\line2""")
    assertTrue(result.contains("<br>"))
  }

  @Test
  fun `transpileToHtml converts newline command to br`() {
    val result = transpile("""\newline""")
    assertTrue(result.contains("<br>"))
  }

  @Test
  fun `transpileToHtml converts newpage to hr`() {
    val result = transpile("""\newpage""")
    assertTrue(result.contains("<hr>"))
  }

  @Test
  fun `transpileToHtml converts hrule to hr`() {
    val result = transpile("""\hrule""")
    assertTrue(result.contains("<hr>"))
  }

  @Test
  fun `transpileToHtml strips document preamble before begin document`() {
    val latex = """\documentclass{article}
\usepackage{amsmath}
\begin{document}
Actual content
\end{document}"""
    val result = transpile(latex)
    assertTrue(result.contains("Actual content"))
    assertFalse(result.contains("""\documentclass"""))
    assertFalse(result.contains("""\begin{document}"""))
    assertFalse(result.contains("""\end{document}"""))
  }

  @Test
  fun `transpileToHtml removes maketitle command`() {
    val result = transpile("""\maketitle""")
    assertFalse(result.contains("""\maketitle"""))
  }

  @Test
  fun `transpileToHtml removes unknown commands`() {
    val result = transpile("""\someUnknownCommand""")
    assertFalse(result.contains("""\someUnknownCommand"""))
  }

  @Test
  fun `transpileToHtml converts par to double br`() {
    val result = transpile("""\par""")
    assertTrue(result.contains("<br>"))
  }
}
