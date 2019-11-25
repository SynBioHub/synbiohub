# --------------------------------------------------------------------
# Makefile for University of Utah Sample Thesis
#
#	all	 make dvi file
#	pdf	 make pdf file
#	ps	 make postscript file
#	clean	 remove intermediate files
#	clobber	 remove everything
#	check	 doubled word and latex syntax check
#
# [28-May-2001] -- update for LaTeX2e
# [06-Jul-1993] 
# --------------------------------------------------------------------

PDFLATEX = pdflatex
BIBTEX = bibtex
DETEX = detex -n
LACHECK = lacheck
DISTILL = ps2pdf -dMaxSubsetPct=100 -dCompatibilityLevel=1.3
DVIPS = dvips -Ppdf -tletter
FIG2EPS = fig2dev -Leps
DW = dw # ftp://ftp.math.utah.edu/pub/misc/dw.tar.gz
CP = cp
RM = rm -f
SED = sed
#SHELL = /bin/sh
SHELL = /bin/bash

#TEXFILES = \
	abstract.tex \
	appendix.tex \
	acknowledge.tex \
	chap1.tex \
	chap2.tex \
	chap3.tex \
	libsbolj-crisprTutorial-2-2.tex

TEXFILES = libsbolj-crisprTutorial-2-2.tex

FIGURES = 

FIGFILES =

EPSFILES = \
	$(FIGFILES:.fig=.eps)

# --------------------------------------------------------------------

%.eps: %.fig
	$(FIG2EPS) $< >$@

all: libsbolj-crisprTutorial-2-2.dvi

pdf: libsbolj-crisprTutorial-2-2.pdf

ps: libsbolj-crisprTutorial-2-2.ps

check: dw syn

#libsbolj-crisprTutorial-2-2.dvi: libsbolj-crisprTutorial-2-2.tex aliascnt.sty $(FIGURES) $(TEXFILES) $(EPSFILES)
libsbolj-crisprTutorial-2-2.dvi: libsbolj-crisprTutorial-2-2.tex $(FIGURES) $(TEXFILES) $(EPSFILES)
	@echo '-------------------- latex #1 --------------------'
	$(PDFLATEX) --shell-escape libsbolj-crisprTutorial-2-2.tex
	@echo '-------------------- latex #2 --------------------'
	$(PDFLATEX) --shell-escape libsbolj-crisprTutorial-2-2.tex
	$(BIBTEX) libsbolj-crisprTutorial-2-2
	@echo '-------------------- latex #3 --------------------'
	$(PDFLATEX) --shell-escape libsbolj-crisprTutorial-2-2.tex

libsbolj-crisprTutorial-2-2.pdf: libsbolj-crisprTutorial-2-2.ps
	@echo '-------------------- pdf --------------------'
	$(DISTILL) $?

libsbolj-crisprTutorial-2-2.ps: libsbolj-crisprTutorial-2-2.dvi
	@echo '-------------------- ps --------------------'
	$(DVIPS) -o $@ $?

dw:
	@-$(RM) libsbolj-crisprTutorial-2-2.dw
	@echo '-------------------- doubled words --------------------'
	@for f in $(TEXFILES) ;\
	do \
		echo ----- $$f ----- ; \
		echo ----- $$f ----- >> libsbolj-crisprTutorial-2-2.dw ; \
		$(DETEX) $$f  | $(DW) >> libsbolj-crisprTutorial-2-2.dw ; \
	done

syn:
	@echo '-------------------- syntax check --------------------'
	@$(LACHECK) libsbolj-crisprTutorial-2-2.tex

clean:
	@$(RM) $(FIGFILES:.fig=.eps) *.blg *.dvi* *.dw *.ilg \
	*.log *.o *.tmp *.old *~ \#* core

clobber: clean
	@$(RM) *.aux *.bbl *.idx *.ind \
	*.toc *.lof *.lot *.pdf *.ps
