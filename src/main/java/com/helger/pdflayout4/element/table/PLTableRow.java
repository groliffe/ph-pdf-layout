/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.pdflayout4.element.table;

import java.awt.Color;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.helger.commons.mutable.MutableInt;
import com.helger.commons.state.EChange;
import com.helger.pdflayout4.base.AbstractPLRenderableObject;
import com.helger.pdflayout4.base.IPLSplittableObject;
import com.helger.pdflayout4.base.IPLVisitor;
import com.helger.pdflayout4.base.PLSplitResult;
import com.helger.pdflayout4.element.hbox.PLHBox;
import com.helger.pdflayout4.element.hbox.PLHBoxColumn;
import com.helger.pdflayout4.render.PageRenderContext;
import com.helger.pdflayout4.render.PreparationContext;
import com.helger.pdflayout4.spec.BorderStyleSpec;
import com.helger.pdflayout4.spec.SizeSpec;
import com.helger.pdflayout4.spec.WidthSpec;

public class PLTableRow extends AbstractPLRenderableObject <PLTableRow> implements IPLSplittableObject <PLTableRow>
{
  private final PLHBox m_aRow = new PLHBox ().setVertSplittable (true);
  private boolean m_bSeparableFromNextRow = true;

  public PLTableRow ()
  {}

  public void addCell (@Nonnull final PLTableCell aCell, @Nonnull final WidthSpec aWidth)
  {
    m_aRow.addColumn (aCell, aWidth);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  protected void onAfterSetID ()
  {
    m_aRow.setID (getID () + "-hbox");
  }

  @Nonnull
  @OverridingMethodsMustInvokeSuper
  public PLTableRow setBasicDataFrom (@Nonnull final PLTableRow aSource)
  {
    super.setBasicDataFrom (aSource);
    m_aRow.setBasicDataFrom (aSource.m_aRow);
    return this;
  }

  @Nullable
  public PLTableCell getCellAtIndex (final int nIndex)
  {
    final PLHBoxColumn aColumn = m_aRow.getColumnAtIndex (nIndex);
    return aColumn == null ? null : (PLTableCell) aColumn.getElement ();
  }

  @Nonnegative
  public int getCellCount ()
  {
    return m_aRow.getColumnCount ();
  }

  @Nullable
  public PLTableCell getFirstCell ()
  {
    return getCellAtIndex (0);
  }

  @Nullable
  public PLTableCell getLastCell ()
  {
    return getCellAtIndex (getCellCount () - 1);
  }

  public void forEachCell (@Nonnull final Consumer <? super PLTableCell> aConsumer)
  {
    m_aRow.forEachColumn (x -> aConsumer.accept ((PLTableCell) x.getElement ()));
  }

  public void forEachCell (@Nonnull final ObjIntConsumer <? super PLTableCell> aConsumer)
  {
    m_aRow.forEachColumn ( (x, idx) -> aConsumer.accept ((PLTableCell) x.getElement (), idx));
  }

  public void forEachCell (@Nonnull final IPLTableCellConsumer aConsumer)
  {
    final MutableInt aEffectiveIndex = new MutableInt (0);
    m_aRow.forEachColumn ( (x, idx) -> {
      final PLTableCell aCell = (PLTableCell) x.getElement ();
      final int nColSpan = aCell.getColSpan ();
      aConsumer.accept (aCell, idx, aEffectiveIndex.intValue (), aEffectiveIndex.intValue () + nColSpan);
      aEffectiveIndex.inc (nColSpan);
    });
  }

  public void forEachCell (final int nStartIncl,
                           final int nEndIncl,
                           @Nonnull final Consumer <? super PLTableCell> aConsumer)
  {
    forEachCell ( (x, idx) -> {
      if (idx >= nStartIncl && idx <= nEndIncl)
        aConsumer.accept (x);
    });
  }

  public void forEachCell (final int nStartIncl,
                           final int nEndIncl,
                           @Nonnull final ObjIntConsumer <? super PLTableCell> aConsumer)
  {
    forEachCell ( (x, idx) -> {
      if (idx >= nStartIncl && idx <= nEndIncl)
        aConsumer.accept (x, idx);
    });
  }

  public void forEachCell (@Nonnull final IPLTableCellFilter aFilter, @Nonnull final IPLTableCellConsumer aConsumer)
  {
    forEachCell ( (x, idx, esidx, eeidx) -> {
      if (aFilter.test (x, idx, esidx, eeidx))
        aConsumer.accept (x, idx, esidx, eeidx);
    });
  }

  @Nonnull
  public PLTableRow setFillColor (@Nullable final Color aFillColor)
  {
    forEachCell (x -> x.setFillColor (aFillColor));
    return this;
  }

  @Nonnull
  public PLTableRow setBorderTop (@Nullable final BorderStyleSpec aBorder)
  {
    forEachCell (x -> x.setBorderTop (aBorder));
    return this;
  }

  @Nonnull
  public PLTableRow setBorderBottom (@Nullable final BorderStyleSpec aBorder)
  {
    forEachCell (x -> x.setBorderBottom (aBorder));
    return this;
  }

  @Nonnull
  public PLTableRow setMarginTop (final float fMargin)
  {
    forEachCell (x -> x.setMarginTop (fMargin));
    return this;
  }

  @Nonnull
  public PLTableRow setMarginBottom (final float fMargin)
  {
    forEachCell (x -> x.setMarginBottom (fMargin));
    return this;
  }

  @Nonnull
  public PLTableRow setPaddingTop (final float fPadding)
  {
    forEachCell (x -> x.setPaddingTop (fPadding));
    return this;
  }

  @Nonnull
  public PLTableRow setPaddingBottom (final float fPadding)
  {
    forEachCell (x -> x.setPaddingBottom (fPadding));
    return this;
  }

  public boolean isSeparableFromNextRow ()
  {
    return m_bSeparableFromNextRow;
  }

  @Nonnull
  public PLTableRow setSeparableFromNextRow (final boolean bSeparableFromNextRow)
  {
    m_bSeparableFromNextRow = bSeparableFromNextRow;
    return this;
  }

  @Override
  @Nonnull
  public EChange visit (@Nonnull final IPLVisitor aVisitor) throws IOException
  {
    EChange ret = super.visit (aVisitor);
    ret = ret.or (m_aRow.visit (aVisitor));
    return ret;
  }

  @Override
  protected SizeSpec onPrepare (final PreparationContext aCtx)
  {
    return m_aRow.prepare (aCtx);
  }

  @Override
  protected void onMarkAsNotPrepared ()
  {
    m_aRow.internalMarkAsNotPrepared ();
  }

  public boolean isVertSplittable ()
  {
    return m_aRow.isVertSplittable ();
  }

  @Nullable
  public PLSplitResult splitElementVert (final float fAvailableWidth, final float fAvailableHeight)
  {
    return m_aRow.splitElementVert (fAvailableWidth, fAvailableHeight);
  }

  @Override
  protected void onRender (final PageRenderContext aCtx) throws IOException
  {
    m_aRow.render (aCtx);
  }
}
