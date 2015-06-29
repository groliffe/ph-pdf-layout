/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.pdflayout;

import java.awt.Color;

import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.helger.commons.io.file.FileHelper;
import com.helger.commons.junit.DebugModeTestRule;
import com.helger.pdflayout.element.PLHBox;
import com.helger.pdflayout.element.PLPageSet;
import com.helger.pdflayout.element.PLSpacerX;
import com.helger.pdflayout.element.PLSpacerY;
import com.helger.pdflayout.element.PLText;
import com.helger.pdflayout.element.PLTextWithPlaceholders;
import com.helger.pdflayout.element.PLVBox;
import com.helger.pdflayout.render.IRenderingContextCustomizer;
import com.helger.pdflayout.render.RenderPageIndex;
import com.helger.pdflayout.render.RenderingContext;
import com.helger.pdflayout.spec.BorderStyleSpec;
import com.helger.pdflayout.spec.EHorzAlignment;
import com.helger.pdflayout.spec.FontSpec;
import com.helger.pdflayout.spec.LineDashPatternSpec;
import com.helger.pdflayout.spec.PDFFont;
import com.helger.pdflayout.spec.WidthSpec;

/**
 * Test class for class {@link PageLayoutPDF}.
 *
 * @author Philip Helger
 */
public final class PageLayoutPDFTest
{
  @Rule
  public TestRule m_aRule = new DebugModeTestRule ();

  @Test
  public void testBasic () throws PDFCreationException
  {
    final FontSpec r10 = new FontSpec (PDFFont.REGULAR, 10);
    final FontSpec r12 = r10.getCloneWithDifferentFontSize (12);
    final PLPageSet aPS1 = new PLPageSet (PDPage.PAGE_SIZE_A4).setMargin (30, 50).setPadding (15);
    aPS1.setPageHeader (new PLText ("Das ist die Kopfzeile", r10).setBorderBottom (new BorderStyleSpec (Color.BLACK))
                                                                 .setHorzAlign (EHorzAlignment.CENTER));
    aPS1.setPageFooter (new PLTextWithPlaceholders ("Das ist die Fusszeile, Seite " +
                                                    RenderPageIndex.PLACEHOLDER_TOTAL_PAGE_NUMBER +
                                                    " von " +
                                                    RenderPageIndex.PLACEHOLDER_TOTAL_PAGE_COUNT, r10).setBorderTop (new BorderStyleSpec (Color.BLACK))
                                                                                                      .setHorzAlign (EHorzAlignment.CENTER));
    aPS1.addElement (new PLText ("Zeile 1", r10));
    {
      final PLHBox aHBox = new PLHBox ();
      // First column 30%
      aHBox.addColumn (new PLText ("Spalte 1 mit Text Spalte 1 mit Text Spalte 1 mit Text Spalte 1 mit Text Spalte 1 mit Text Spalte 1 mit Text Spalte 1 mit Text Spalte 1 mit Text Spalte 1 mit Text Spalte 1 mit Text Spalte 1 mit Text Spalte 1 mit Text ",
                                   r10).setMargin (10)
                                       .setPadding (5)
                                       .setHorzAlign (EHorzAlignment.LEFT)
                                       .setBorder (new BorderStyleSpec (Color.RED),
                                                   new BorderStyleSpec (Color.GREEN),
                                                   new BorderStyleSpec (Color.BLUE),
                                                   new BorderStyleSpec (Color.CYAN)),
                       WidthSpec.perc (30));
      // Remaining columns use each the same part of the space: WidthSpec.star()
      aHBox.addColumn (new PLText ("Spalte 2 mit Text Spalte 2 mit Text Spalte 2 mit Text Spalte 2 mit Text Spalte 2 mit Text Spalte 2 mit Text Spalte 2 mit Text Spalte 2 mit Text Spalte 2 mit Text Spalte 2 mit Text Spalte 2 mit Text Spalte 2 mit Text ",
                                   r10.getCloneWithDifferentColor (Color.BLUE)).setBorder (new BorderStyleSpec (Color.RED))
                                                                               .setHorzAlign (EHorzAlignment.CENTER)
                                                                               .setMaxRows (3),
                       WidthSpec.star ());
      aHBox.addColumn (new PLText ("Spalte 3 mit Text Spalte 3 mit Text Spalte 3 mit Text Ende", r10).setMarginTop (10)
                                                                                                     .setPadding (5)
                                                                                                     .setBorder (new BorderStyleSpec (Color.GREEN,
                                                                                                                                      LineDashPatternSpec.DASHED_3))
                                                                                                     .setHorzAlign (EHorzAlignment.RIGHT),
                       WidthSpec.star ());
      aHBox.addColumn (new PLText ("Spalte 4 mit Text Spalte 4 mit Text Spalte 4 mit Text Ende",
                                   r10.getCloneWithDifferentFont (PDFFont.REGULAR_ITALIC)).setBorder (new BorderStyleSpec (Color.RED))
                                                                                          .setHorzAlign (EHorzAlignment.LEFT),
                       WidthSpec.star ());
      aPS1.addElement (aHBox);
    }
    {
      final PLHBox h = new PLHBox ().setColumnBorder (new BorderStyleSpec (Color.RED))
                                    .setColumnFillColor (new Color (0xbbbbbb));
      h.addColumn (new PLText ("Column 1", r10.getCloneWithDifferentFontSize (24)).setHorzAlign (EHorzAlignment.CENTER),
                   WidthSpec.star ());
      final PLVBox v = new PLVBox ().setRowBorder (new BorderStyleSpec (Color.GREEN));
      v.addRow (new PLText ("Column 2, Row 1", r10));
      v.addRow (new PLText ("Column 2, Row 2", r12.getCloneWithDifferentColor (Color.RED)).setFillColor (new Color (0xdddddd)));
      v.addRow (new PLText ("Column 2, Row 3", r12));
      h.addColumn (v, WidthSpec.star ());
      final PLVBox v2 = new PLVBox ();
      v2.addRow (new PLText ("Column 3, Row 1", r10));
      v2.addRow (new PLText ("Column 3, Row 2", r10));
      v2.addRow (new PLText ("Column 3, Row 3", r10).setFillColor (new Color (0xdddddd)));
      h.addColumn (v2, WidthSpec.star ());
      aPS1.addElement (h);
    }
    aPS1.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r10).setHorzAlign (EHorzAlignment.CENTER)
                                     .setBorder (new BorderStyleSpec (Color.BLACK)));
    aPS1.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r12).setHorzAlign (EHorzAlignment.RIGHT)
                                     .setFillColor (new Color (0xff0000))
                                     .setPadding (5, 0));
    aPS1.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 new FontSpec (PDFFont.MONOSPACE, 14)));
    aPS1.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r12).setHorzAlign (EHorzAlignment.CENTER));
    aPS1.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r10).setHorzAlign (EHorzAlignment.RIGHT));
    aPS1.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r12));
    aPS1.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r10).setHorzAlign (EHorzAlignment.CENTER));
    aPS1.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r12).setHorzAlign (EHorzAlignment.RIGHT));
    aPS1.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r10));
    aPS1.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r12).setHorzAlign (EHorzAlignment.CENTER));

    final PLPageSet aPS2 = new PLPageSet (PDPage.PAGE_SIZE_A4.getWidth (), PDPage.PAGE_SIZE_A4.getWidth ()).setMargin (30,
                                                                                                                       50)
                                                                                                           .setPadding (15);
    aPS2.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r10).setHorzAlign (EHorzAlignment.RIGHT));
    aPS2.addElement (new PLText ("Zeile 2\nZeile 3\nTäst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort Täst Täst Täst Täst Täst Tästlangeswort ",
                                 r12));

    final PLPageSet aPS3 = new PLPageSet (PDPage.PAGE_SIZE_A4.getHeight (), PDPage.PAGE_SIZE_A4.getWidth ()).setMargin (30,
                                                                                                                        50)
                                                                                                            .setPadding (15);
    aPS3.setRenderingContextCustomizer (new IRenderingContextCustomizer ()
    {
      public void customizeRenderingContext (final RenderingContext aRC)
      {
        final int nTotal = aRC.getPlaceholderAsInt (RenderPageIndex.PLACEHOLDER_TOTAL_PAGE_COUNT, -1);
        aRC.setPlaceholder ("${pages-1}", nTotal - 1);
      }
    });
    aPS3.setPageHeader (new PLText ("Das ist die Kopfzeile3", r10).setBorderBottom (new BorderStyleSpec (Color.BLACK))
                                                                  .setHorzAlign (EHorzAlignment.CENTER));
    aPS3.setPageFooter (new PLTextWithPlaceholders ("Das ist die Fusszeile3, Seite " +
                                                    RenderPageIndex.PLACEHOLDER_PAGESET_PAGE_NUMBER +
                                                    " von " +
                                                    RenderPageIndex.PLACEHOLDER_PAGESET_PAGE_COUNT +
                                                    " bzw. " +
                                                    RenderPageIndex.PLACEHOLDER_TOTAL_PAGE_NUMBER +
                                                    " von " +
                                                    RenderPageIndex.PLACEHOLDER_TOTAL_PAGE_COUNT +
                                                    "; my value: ${pages-1}", r10).setBorderTop (new BorderStyleSpec (Color.BLACK))
                                                                                  .setHorzAlign (EHorzAlignment.CENTER));
    aPS3.addElement (new PLText ("Zeile 1\n\nZeile 3", r10));

    final PageLayoutPDF aPageLayout = new PageLayoutPDF ().setDebug (false);
    aPageLayout.addPageSet (aPS1);
    aPageLayout.addPageSet (aPS2);
    aPageLayout.addPageSet (aPS3);
    aPageLayout.renderTo (FileHelper.getOutputStream ("pdf/test1.pdf"));
  }

  @Test
  public void testCreatePDFProperties () throws PDFCreationException
  {
    final FontSpec r10 = new FontSpec (PDFFont.REGULAR, 10);

    final PLPageSet aPS1 = new PLPageSet (PDPage.PAGE_SIZE_A4).setMargin (30);
    aPS1.addElement (new PLText ("Dummy line", r10));

    final PageLayoutPDF aPageLayout = new PageLayoutPDF ().setDebug (false);
    aPageLayout.setDocumentAuthor ("Weird author äöü");
    aPageLayout.setDocumentTitle ("Special chars €!\"§$%&/()=\uFFE5");
    aPageLayout.addPageSet (aPS1);
    aPageLayout.renderTo (FileHelper.getOutputStream ("pdf/test-properties.pdf"));
  }

  @Test
  public void testPageLayoutPDFMarginPadding () throws PDFCreationException
  {
    final String sLID = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
    final String sLIDShort = sLID.substring (0, sLID.length () / 3);

    final FontSpec r10 = new FontSpec (PDFFont.REGULAR, 10);
    final PLPageSet aPS1 = new PLPageSet (PDPage.PAGE_SIZE_A4).setMargin (10, 20, 30, 40)
                                                              .setPadding (10, 20, 30, 40)
                                                              .setFillColor (new Color (0xeeeeee));
    aPS1.setPageHeader (new PLText ("Headline", r10).setBorder (new BorderStyleSpec (Color.BLACK))
                                                    .setPadding (0, 4)
                                                    .setHorzAlign (EHorzAlignment.CENTER));
    aPS1.setPageFooter (new PLTextWithPlaceholders ("Page " +
                                                    RenderPageIndex.PLACEHOLDER_TOTAL_PAGE_NUMBER +
                                                    " of " +
                                                    RenderPageIndex.PLACEHOLDER_TOTAL_PAGE_COUNT, r10).setBorder (new BorderStyleSpec (Color.BLACK))
                                                                                                      .setMarginTop (10)
                                                                                                      .setPadding (10,
                                                                                                                   4)
                                                                                                      .setHorzAlign (EHorzAlignment.RIGHT));
    {
      final PLHBox h = new PLHBox ().setMargin (0, -20, -30, 10)
                                    .setPadding (5)
                                    .setColumnFillColor (new Color (0xbbbbbb));
      if (false)
        h.setBorder (new BorderStyleSpec (Color.RED)).setColumnBorder (new BorderStyleSpec (Color.GREEN));

      h.addColumn (new PLText (sLID, r10).setHorzAlign (EHorzAlignment.CENTER).setPadding (20, 0), WidthSpec.star ());

      final PLVBox v1 = new PLVBox ().setPadding (5).setFillColor (new Color (0xabcdef));
      if (false)
        v1.setBorder (new BorderStyleSpec (Color.RED)).setRowBorder (new BorderStyleSpec (Color.GREEN));
      v1.addRow (new PLText (sLIDShort, r10).setMargin (0).setPadding (10).setBorder (new BorderStyleSpec (Color.BLUE)));
      v1.addRow (new PLText (sLIDShort, r10).setMargin (5)
                                            .setPadding (5)
                                            .setBorder (new BorderStyleSpec (Color.BLUE, LineDashPatternSpec.DASHED_2)));
      v1.addRow (new PLText (sLIDShort, r10).setMargin (10).setPadding (0).setBorder (new BorderStyleSpec (Color.BLUE)));
      h.addColumn (v1, WidthSpec.star ());

      final PLVBox v2 = new PLVBox ();
      v2.addRow (new PLText (sLIDShort, r10).setHorzAlign (EHorzAlignment.RIGHT));
      v2.addRow (new PLText (sLIDShort, r10).setHorzAlign (EHorzAlignment.RIGHT).setFillColor (new Color (0xdddddd)));
      v2.addRow (new PLText (sLIDShort, r10).setHorzAlign (EHorzAlignment.RIGHT));
      h.addColumn (v2, WidthSpec.star ());
      aPS1.addElement (h);
    }
    if (true)
    {
      aPS1.addElement (new PLText (sLID, r10.getCloneWithDifferentColor (Color.WHITE)).setHorzAlign (EHorzAlignment.RIGHT)
                                                                                      .setBorder (new BorderStyleSpec (Color.BLACK))
                                                                                      .setFillColor (Color.RED)
                                                                                      .setMargin (0)
                                                                                      .setPadding (10));
      aPS1.addElement (new PLText (sLID, r10.getCloneWithDifferentColor (Color.RED)).setHorzAlign (EHorzAlignment.RIGHT)
                                                                                    .setBorder (new BorderStyleSpec (Color.BLACK))
                                                                                    .setFillColor (Color.GREEN)
                                                                                    .setMargin (5)
                                                                                    .setPadding (5));
      aPS1.addElement (new PLText (sLID, r10.getCloneWithDifferentColor (Color.WHITE)).setHorzAlign (EHorzAlignment.RIGHT)
                                                                                      .setBorder (new BorderStyleSpec (Color.BLACK))
                                                                                      .setFillColor (Color.BLUE)
                                                                                      .setMargin (10)
                                                                                      .setPadding (0));
      aPS1.addElement (new PLText (sLID, r10).setFillColor (new Color (0xabcdef)));
    }

    final PageLayoutPDF aPageLayout = new PageLayoutPDF ().setDebug (false);
    aPageLayout.addPageSet (aPS1);
    aPageLayout.renderTo (FileHelper.getOutputStream ("pdf/test2.pdf"));
  }

  @Test
  public void testDINLetter () throws PDFCreationException
  {
    final FontSpec r10 = new FontSpec (PDFFont.REGULAR, 10);

    final PLPageSet aPS1 = new PLPageSet (PDPage.PAGE_SIZE_A4);
    {
      final PLVBox aVBox = new PLVBox ();
      final PLHBox aHBox = new PLHBox ();
      aHBox.addColumn (new PLSpacerX (), WidthSpec.abs (PLConvert.mm2units (20)));
      {
        final PLVBox aWindow = new PLVBox ();
        aWindow.addRow (new PLSpacerY (PLConvert.mm2units (42)));
        aWindow.addRow (new PLText ("Hr. MaxMustermann\nMusterstraße 15\nA-1010 Wien", r10).setExactSize (PLConvert.mm2units (90),
                                                                                                          PLConvert.mm2units (45)));
        aWindow.addRow (new PLSpacerY (PLConvert.mm2units (12)));
        aHBox.addColumn (aWindow, WidthSpec.abs (PLConvert.mm2units (90)));
      }
      aHBox.addColumn (new PLSpacerX (), WidthSpec.star ());
      aVBox.addRow (aHBox);
      aPS1.addElement (aVBox);
    }
    aPS1.addElement (new PLSpacerY (PLConvert.mm2units (99)));
    aPS1.addElement (new PLSpacerY (PLConvert.mm2units (98.5f)));

    final PageLayoutPDF aPageLayout = new PageLayoutPDF ().setDebug (true);
    aPageLayout.addPageSet (aPS1);
    aPageLayout.renderTo (FileHelper.getOutputStream ("pdf/test-din-letter.pdf"));
  }
}
