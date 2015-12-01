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
package com.helger.pdflayout.spec;

import java.awt.Color;
import java.io.IOException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.MustImplementEqualsAndHashcode;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * Defines a text font specification containing the font, the font size and the
 * text color.
 *
 * @author Philip Helger
 */
@Immutable
@MustImplementEqualsAndHashcode
public class FontSpec
{
  /** The default font color: black */
  public static final Color DEFAULT_COLOR = Color.BLACK;

  private final PreloadFont m_aFont;
  private final float m_fFontSize;
  private final Color m_aColor;
  private LoadedFont m_aLoadedFont;

  public FontSpec (@Nonnull final PreloadFont aFont, @Nonnegative final float fFontSize)
  {
    this (aFont, fFontSize, DEFAULT_COLOR);
  }

  public FontSpec (@Nonnull final PreloadFont aFont, @Nonnegative final float fFontSize, @Nonnull final Color aColor)
  {
    ValueEnforcer.notNull (aFont, "Font");
    ValueEnforcer.isGT0 (fFontSize, "FontSize");
    ValueEnforcer.notNull (aColor, "Color");
    m_aFont = aFont;
    m_fFontSize = fFontSize;
    m_aColor = aColor;
  }

  /**
   * @return The font to use. Never <code>null</code>.
   */
  @Nonnull
  public PreloadFont getPreloadFont ()
  {
    return m_aFont;
  }

  /**
   * @return The font size in points. Always &gt; 0.
   */
  @Nonnegative
  public float getFontSize ()
  {
    return m_fFontSize;
  }

  /**
   * @return The text color to use.
   */
  @Nonnull
  public Color getColor ()
  {
    return m_aColor;
  }

  /**
   * @param aDoc
   *        The PDDocument for which the font should be loaded.
   * @return The loaded font or the cached value.
   * @throws IOException
   *         In case font loading fails
   */
  @Nonnull
  public LoadedFont getAsLoadedFont (@Nonnull final PDDocument aDoc) throws IOException
  {
    // Cache to avoid parsing TTF over and over again
    if (m_aLoadedFont == null)
      m_aLoadedFont = new LoadedFont (m_aFont.getAsPDFont (aDoc));
    return m_aLoadedFont;
  }

  /**
   * Return a clone of this object but with a different font.
   *
   * @param aNewFont
   *        The new font to use. Must not be <code>null</code>.
   * @return this if the fonts are equal - a new object otherwise.
   */
  @Nonnull
  public FontSpec getCloneWithDifferentFont (@Nonnull final PreloadFont aNewFont)
  {
    ValueEnforcer.notNull (aNewFont, "NewFont");
    if (aNewFont.equals (m_aFont))
      return this;
    return new FontSpec (aNewFont, m_fFontSize, m_aColor);
  }

  /**
   * Return a clone of this object but with a different font size.
   *
   * @param fNewFontSize
   *        The new font size to use. Must be &gt; 0.
   * @return this if the font sizes are equal - a new object otherwise.
   */
  @Nonnull
  public FontSpec getCloneWithDifferentFontSize (final float fNewFontSize)
  {
    ValueEnforcer.isGT0 (fNewFontSize, "FontSize");
    if (EqualsHelper.equals (fNewFontSize, m_fFontSize))
      return this;
    return new FontSpec (m_aFont, fNewFontSize, m_aColor);
  }

  /**
   * Return a clone of this object but with a different color.
   *
   * @param aNewColor
   *        The new color to use. May not be <code>null</code>.
   * @return this if the colors are equal - a new object otherwise.
   */
  @Nonnull
  public FontSpec getCloneWithDifferentColor (@Nonnull final Color aNewColor)
  {
    ValueEnforcer.notNull (aNewColor, "NewColor");
    if (aNewColor.equals (m_aColor))
      return this;
    return new FontSpec (m_aFont, m_fFontSize, aNewColor);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final FontSpec rhs = (FontSpec) o;
    return m_aFont.equals (rhs.m_aFont) && EqualsHelper.equals (m_fFontSize, rhs.m_fFontSize) && m_aColor.equals (rhs.m_aColor);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aFont).append (m_fFontSize).append (m_aColor).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("font", m_aFont).append ("fontSize", m_fFontSize).append ("color", m_aColor).toString ();
  }
}
