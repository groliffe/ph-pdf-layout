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

import static org.junit.Assert.assertEquals;

import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Test;

/**
 * Test class for class {@link CPageLayoutPDF}.
 *
 * @author Philip Helger
 */
public final class CPageLayoutPDFTest
{
  @Test
  public void testBasic ()
  {
    assertEquals (PDPage.PAGE_SIZE_A4.getWidth (), CPageLayoutPDF.mm2units (210), 0.001f);
    assertEquals (PDPage.PAGE_SIZE_A4.getWidth (), CPageLayoutPDF.cm2units (21), 0.001f);
  }
}