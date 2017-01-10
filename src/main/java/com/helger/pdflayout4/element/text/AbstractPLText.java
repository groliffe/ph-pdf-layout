/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.pdflayout4.element.text;

import java.io.IOException;
import java.util.List;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.state.EChange;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.pdflayout4.PLDebug;
import com.helger.pdflayout4.base.AbstractPLInlineElement;
import com.helger.pdflayout4.base.IPLHasHorizontalAlignment;
import com.helger.pdflayout4.base.IPLSplittableObject;
import com.helger.pdflayout4.base.PLElementWithSize;
import com.helger.pdflayout4.base.PLSplitResult;
import com.helger.pdflayout4.element.PLRenderHelper;
import com.helger.pdflayout4.pdfbox.PDPageContentStreamWithCache;
import com.helger.pdflayout4.render.PagePreRenderContext;
import com.helger.pdflayout4.render.PageRenderContext;
import com.helger.pdflayout4.render.PreparationContext;
import com.helger.pdflayout4.spec.EHorzAlignment;
import com.helger.pdflayout4.spec.FontSpec;
import com.helger.pdflayout4.spec.LoadedFont;
import com.helger.pdflayout4.spec.SizeSpec;
import com.helger.pdflayout4.spec.TextAndWidthSpec;

/**
 * Render text
 *
 * @author Philip Helger
 * @param <IMPLTYPE>
 *        Implementation type
 */
public abstract class AbstractPLText <IMPLTYPE extends AbstractPLText <IMPLTYPE>>
                                     extends AbstractPLInlineElement <IMPLTYPE>
                                     implements IPLHasHorizontalAlignment <IMPLTYPE>, IPLSplittableObject <IMPLTYPE>
{
  public static final int DEFAULT_MAX_ROWS = CGlobal.ILLEGAL_UINT;
  public static final boolean DEFAULT_REPLACE_PLACEHOLDERS = false;

  private String m_sOriginalText;
  private String m_sResolvedText;
  private final FontSpec m_aFontSpec;

  private EHorzAlignment m_eHorzAlign = DEFAULT_HORZ_ALIGNMENT;
  private int m_nMaxRows = DEFAULT_MAX_ROWS;
  private boolean m_bVertSplittable = DEFAULT_VERT_SPLITTABLE;
  private boolean m_bReplacePlaceholder = DEFAULT_REPLACE_PLACEHOLDERS;

  // prepare result
  private transient LoadedFont m_aLoadedFont;
  protected float m_fTextHeight;
  protected float m_fDescent;

  protected int m_nPreparedLineCountUnmodified = CGlobal.ILLEGAL_UINT;
  protected ICommonsList <TextAndWidthSpec> m_aPreparedLinesUnmodified;
  protected ICommonsList <TextAndWidthSpec> m_aPreparedLines;

  @Nonnull
  public static String getCleanedPLText (@Nullable final String sText)
  {
    if (StringHelper.hasNoText (sText))
    {
      return "";
    }
    // Unify line endings so that all "\r" are removed and only "\n" is
    // contained
    // Multiple \n after each other remain
    String sCleaned = sText;
    sCleaned = StringHelper.replaceAll (sCleaned, "\r\n", "\n");
    sCleaned = StringHelper.replaceAll (sCleaned, '\r', '\n');
    return sCleaned;
  }

  public AbstractPLText (@Nullable final String sText, @Nonnull final FontSpec aFontSpec)
  {
    _setText (sText);
    m_aFontSpec = ValueEnforcer.notNull (aFontSpec, "FontSpec");
  }

  /**
   * Set the internal text fields
   *
   * @param sText
   *        Text to use. May be <code>null</code>.
   */
  private void _setText (@Nullable final String sText)
  {
    m_sOriginalText = getCleanedPLText (sText);
    m_sResolvedText = m_sOriginalText;
  }

  @Override
  @Nonnull
  @OverridingMethodsMustInvokeSuper
  public IMPLTYPE setBasicDataFrom (@Nonnull final IMPLTYPE aSource)
  {
    super.setBasicDataFrom (aSource);
    setHorzAlign (aSource.getHorzAlign ());
    setMaxRows (aSource.getMaxRows ());
    setVertSplittable (aSource.isVertSplittable ());
    setReplacePlaceholder (aSource.isReplacePlaceholder ());
    return thisAsT ();
  }

  /**
   * @return The original text provided in the constructor, with newlines
   *         unified. Never <code>null</code>.
   */
  @Nonnull
  public final String getText ()
  {
    return m_sOriginalText;
  }

  /**
   * @return <code>true</code> if the contained text has at least one character,
   *         <code>false</code> if it is empty.
   */
  public final boolean hasText ()
  {
    return m_sOriginalText.length () > 0;
  }

  /**
   * @return <code>true</code> if the text provided in the constructor contains
   *         no character, <code>false</code> otherwise.
   */
  public final boolean hasNoText ()
  {
    return m_sOriginalText.length () == 0;
  }

  /**
   * @return The font specification to be used as provided in the constructor.
   *         Never <code>null</code>.
   */
  @Nonnull
  public final FontSpec getFontSpec ()
  {
    return m_aFontSpec;
  }

  @Nonnull
  public final EHorzAlignment getHorzAlign ()
  {
    return m_eHorzAlign;
  }

  @Nonnull
  public final IMPLTYPE setHorzAlign (@Nonnull final EHorzAlignment eHorzAlign)
  {
    m_eHorzAlign = ValueEnforcer.notNull (eHorzAlign, "HorzAlign");
    return thisAsT ();
  }

  /**
   * @return The maximum number of rows to be rendered. If this value is &le; 0
   *         than all rows are rendered. The default value is
   *         {@link #DEFAULT_MAX_ROWS} meaning all rows are rendered.
   */
  @CheckForSigned
  public final int getMaxRows ()
  {
    return m_nMaxRows;
  }

  /**
   * Set the maximum number of rows to render.
   *
   * @param nMaxRows
   *        Maximum number of rows. If &le; 0 than all lines are rendered.
   * @return this for chaining
   */
  @Nonnull
  public final IMPLTYPE setMaxRows (final int nMaxRows)
  {
    m_nMaxRows = nMaxRows;
    return thisAsT ();
  }

  public final boolean isVertSplittable ()
  {
    return m_bVertSplittable;
  }

  /**
   * Change whether this text is splittable or not.
   *
   * @param bVertSplittable
   *        <code>true</code> if this text should be splittable,
   *        <code>false</code> otherwise.
   * @return this for chaining
   */
  @Nonnull
  public final IMPLTYPE setVertSplittable (final boolean bVertSplittable)
  {
    m_bVertSplittable = bVertSplittable;
    return thisAsT ();
  }

  /**
   * @return <code>true</code> if placeholders should be replaced,
   *         <code>false</code> otherwise. The default value is
   *         {@link #DEFAULT_REPLACE_PLACEHOLDERS} so
   *         {@value #DEFAULT_REPLACE_PLACEHOLDERS}.
   */
  public final boolean isReplacePlaceholder ()
  {
    return m_bReplacePlaceholder;
  }

  /**
   * Change whether placeholders should be replaced or not. Enabling this slows
   * down the execution of rendering. Enable this only if absolutely necessary.
   *
   * @param bReplacePlaceholder
   *        <code>true</code> if placeholders should be replaced,
   *        <code>false</code> otherwise.
   * @return this for chaining
   */
  @Nonnull
  public final IMPLTYPE setReplacePlaceholder (final boolean bReplacePlaceholder)
  {
    m_bReplacePlaceholder = bReplacePlaceholder;
    return thisAsT ();
  }

  final void internalSetPreparedLines (@Nonnull final ICommonsList <TextAndWidthSpec> aLines)
  {
    final int nLineCount = aLines.size ();
    m_nPreparedLineCountUnmodified = nLineCount;
    m_aPreparedLinesUnmodified = aLines;
    if (m_nMaxRows <= 0)
    {
      // Use all lines
      m_aPreparedLines = aLines;
    }
    else
    {
      // Use only a certain maximum number of rows
      if (nLineCount <= m_nMaxRows)
      {
        // We have less lines than the maximum
        m_aPreparedLines = aLines;
      }
      else
      {
        // Maximum number of lines exceeded - copy only the relevant lines
        m_aPreparedLines = new CommonsArrayList <> (m_nMaxRows);
        for (int i = 0; i < m_nMaxRows; ++i)
          m_aPreparedLines.add (aLines.get (i));
      }
    }
  }

  final void internalSetPreparedFontData (@Nonnull final LoadedFont aLoadedFont,
                                          final float fTextHeight,
                                          final float fDescent)
  {
    ValueEnforcer.notNull (aLoadedFont, "LoadedFont");
    m_aLoadedFont = aLoadedFont;
    m_fTextHeight = fTextHeight;
    m_fDescent = fDescent;
  }

  /**
   * This method can only be called after loadedFont member was set!
   *
   * @param fAvailableWidth
   *        Available with
   * @return The new preparation size
   * @throws IOException
   *         On PDFBox error
   */
  @Nonnull
  private SizeSpec _prepareText (final float fAvailableWidth) throws IOException
  {
    final float fFontSize = m_aFontSpec.getFontSize ();
    m_fTextHeight = m_aLoadedFont.getTextHeight (fFontSize);
    m_fDescent = m_aLoadedFont.getDescent (fFontSize);

    if (hasNoText ())
    {
      // Nothing to do - empty
      // But keep the height distance!
      return new SizeSpec (0, m_fTextHeight);
    }

    // Split text into rows
    internalSetPreparedLines (m_aLoadedFont.getFitToWidth (m_sResolvedText, fFontSize, fAvailableWidth));

    // Determine max width of all prepared lines
    float fMaxWidth = Float.MIN_VALUE;
    for (final TextAndWidthSpec aTWS : m_aPreparedLines)
      fMaxWidth = Math.max (fMaxWidth, aTWS.getWidth ());

    // Determine height by number of lines
    return new SizeSpec (fMaxWidth, m_aPreparedLines.size () * m_fTextHeight);
  }

  @Override
  protected SizeSpec onPrepare (@Nonnull final PreparationContext aCtx)
  {
    final float fElementWidth = aCtx.getAvailableWidth () - getOutlineXSum ();

    // Load font into document
    try
    {
      m_aLoadedFont = aCtx.getGlobalContext ().getLoadedFont (m_aFontSpec);
      return _prepareText (fElementWidth);
    }
    catch (final IOException ex)
    {
      throw new IllegalStateException ("Failed to prepare text element: " + toString (), ex);
    }
  }

  @Override
  protected void onMarkAsNotPrepared ()
  {
    m_nPreparedLineCountUnmodified = CGlobal.ILLEGAL_UINT;
    m_aPreparedLinesUnmodified = null;
    m_aPreparedLines = null;
  }

  private void _setDisplayTextAfterPrepare (@Nonnull final String sNewResolvedText,
                                            final float fAvailableWidth) throws IOException
  {
    internalMarkAsNotPrepared ();
    m_sResolvedText = sNewResolvedText;
    final SizeSpec aOnPrepareResult = _prepareText (fAvailableWidth);
    internalMarkAsPrepared (aOnPrepareResult);
  }

  /**
   * @return The total number of prepared lines, not taking the maxRows into
   *         consideration. Always &ge; 0.
   */
  @Nonnegative
  public int getPreparedLineCountUnmodified ()
  {
    internalCheckAlreadyPrepared ();
    return m_nPreparedLineCountUnmodified;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <TextAndWidthSpec> getAllPreparedLinesUnmodified ()
  {
    internalCheckAlreadyPrepared ();
    return new CommonsArrayList <> (m_aPreparedLinesUnmodified);
  }

  protected final float getDisplayHeightOfLineCount (@Nonnegative final int nLineCount)
  {
    return nLineCount * m_fTextHeight;
  }

  @Nonnull
  private PLElementWithSize _splitGetCopy (final float fElementWidth,
                                           @Nonnull @Nonempty final List <TextAndWidthSpec> aLines,
                                           final boolean bSplittableCopy,
                                           @Nonnull final String sIDSuffix)
  {
    ValueEnforcer.notEmpty (aLines, "Lines");

    // Create a copy to be independent!
    final ICommonsList <TextAndWidthSpec> aLineCopy = new CommonsArrayList <> (aLines);

    // Excluding padding/margin
    final SizeSpec aSize = new SizeSpec (fElementWidth, getDisplayHeightOfLineCount (aLineCopy.size ()));

    final String sTextContent = StringHelper.getImplodedMapped ('\n', aLineCopy, TextAndWidthSpec::getText);
    final IMPLTYPE aNewText = internalCreateNewVertSplitObject (thisAsT ()).setID (getID () + sIDSuffix);
    ((AbstractPLText <?>) aNewText)._setText (sTextContent);
    // Set this explicitly after setBasicDataFrom!
    aNewText.setVertSplittable (bSplittableCopy);

    // Set min width/max width from source
    // Don't use the height, because on vertically split elements, the height is
    // dynamic
    aNewText.setMinWidth (getMinWidth ());
    aNewText.setMaxWidth (getMaxWidth ());

    aNewText.internalMarkAsPrepared (aSize);
    aNewText.internalSetPreparedLines (aLineCopy);
    aNewText.internalSetPreparedFontData (m_aLoadedFont, m_fTextHeight, m_fDescent);

    return new PLElementWithSize (aNewText, aSize);
  }

  @Nullable
  public PLSplitResult splitElementVert (final float fElementWidth, final float fAvailableHeight)
  {
    if (fAvailableHeight <= 0)
      return null;

    final float fTextHeight = m_fTextHeight;

    // Get the lines in the correct order from top to bottom
    final ICommonsList <TextAndWidthSpec> aLines = m_aPreparedLines;

    int nLines = (int) (fAvailableHeight / fTextHeight);
    if (nLines <= 0)
    {
      // Splitting makes no sense because the resulting text 1 would be empty
      if (PLDebug.isDebugSplit ())
        PLDebug.debugSplit (this,
                            "Failed to split because the result would be " +
                                  nLines +
                                  " lines for available height " +
                                  fAvailableHeight +
                                  " and line height " +
                                  fTextHeight);
      return null;
    }

    if (nLines >= aLines.size ())
    {
      // Splitting makes no sense because the resulting text 2 would be empty
      if (PLDebug.isDebugSplit ())
        PLDebug.debugSplit (this,
                            "Failed to split because the result of " +
                                  nLines +
                                  " lines fits into the available height " +
                                  fAvailableHeight +
                                  " and line height " +
                                  fTextHeight +
                                  " (=" +
                                  (fAvailableHeight * fTextHeight) +
                                  ")");
      return null;
    }

    // Calc estimated height (required because an offset is added)
    final float fExpectedHeight = getDisplayHeightOfLineCount (nLines);
    if (fExpectedHeight > fAvailableHeight)
    {
      // Show one line less
      --nLines;
      if (nLines <= 0)
      {
        // Splitting makes no sense
        if (PLDebug.isDebugSplit ())
          PLDebug.debugSplit (this,
                              "Failed to split because the result would be " +
                                    nLines +
                                    " lines for available height " +
                                    fAvailableHeight +
                                    " and expected height " +
                                    fExpectedHeight);
        return null;
      }
    }

    // First elements does not need to be splittable anymore
    final PLElementWithSize aText1 = _splitGetCopy (fElementWidth, aLines.subList (0, nLines), false, "-1");
    // Second element may need additional splitting
    final PLElementWithSize aText2 = _splitGetCopy (fElementWidth, aLines.subList (nLines, aLines.size ()), true, "-2");

    return new PLSplitResult (aText1, aText2);
  }

  @Override
  @Nonnull
  public EChange beforeRender (@Nonnull final PagePreRenderContext aCtx) throws IOException
  {
    if (m_bReplacePlaceholder)
    {
      final String sOrigText = m_sOriginalText;
      final String sDisplayText = StringHelper.replaceMultiple (sOrigText, aCtx.getAllPlaceholders ());
      if (!sOrigText.equals (sDisplayText))
      {
        // Something changed
        _setDisplayTextAfterPrepare (sDisplayText, getPrepareAvailableSize ().getWidth ());
        return EChange.CHANGED;
      }
    }
    return EChange.UNCHANGED;
  }

  @Override
  protected void onRender (@Nonnull final PageRenderContext aCtx) throws IOException
  {
    if (hasNoText ())
    {
      // Nothing to do - empty text
      return;
    }

    // Fill and border
    PLRenderHelper.fillAndRenderBorder (this, aCtx, 0f, 0f);

    final float fRenderLeft = aCtx.getStartLeft () + getOutlineLeft ();
    final float fRenderTop = aCtx.getStartTop () - getOutlineTop ();

    if (PLDebug.isDebugRender ())
      PLDebug.debugRender (this,
                           "Display at " +
                                 PLDebug.getXYWH (fRenderLeft, fRenderTop, getRenderWidth (), getRenderHeight ()) +
                                 " with " +
                                 m_aPreparedLines.size () +
                                 " lines");

    final PDPageContentStreamWithCache aContentStream = aCtx.getContentStream ();

    aContentStream.beginText ();

    // Set font if changed
    aContentStream.setFont (m_aLoadedFont, m_aFontSpec);

    final float fTextHeight = m_fTextHeight;
    final float fPreparedWidth = getPreparedWidth ();

    int nIndex = 0;
    final int nMax = m_aPreparedLines.size ();
    for (final TextAndWidthSpec aTW : m_aPreparedLines)
    {
      // Replace text (if any)
      final float fTextWidth = aTW.getWidth ();
      final String sDrawText = aTW.getText ();

      // Align text line by overall block width
      final float fIndentX = getIndentX (fPreparedWidth, fTextWidth);
      if (nIndex == 0)
      {
        // Initial move - only partial line height!
        aContentStream.moveTextPositionByAmount (fRenderLeft + fIndentX, fRenderTop - fTextHeight - m_fDescent);
      }
      else
        if (fIndentX != 0)
        {
          // Indent subsequent line
          aContentStream.moveTextPositionByAmount (fIndentX, 0);
        }

      // Main draw string
      aContentStream.drawString (sDrawText);
      ++nIndex;

      // Goto next line
      // Handle indent per-line as when right alignment is used, the indentX may
      // differ from line to line
      if (nIndex < nMax)
      {
        // Outdent and one line down, except for last line
        aContentStream.moveTextPositionByAmount (-fIndentX, -fTextHeight);
      }
    }
    aContentStream.endText ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ())
                            .append ("OriginalText", m_sOriginalText)
                            .append ("ResolvedText", m_sResolvedText)
                            .append ("FontSpec", m_aFontSpec)
                            .append ("HorzAlign", m_eHorzAlign)
                            .append ("MaxRows", m_nMaxRows)
                            .append ("VertSplittable", m_bVertSplittable)
                            .append ("ReplacePlaceholder", m_bReplacePlaceholder)
                            .toString ();
  }
}
