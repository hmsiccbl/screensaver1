// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.derivatives.Derivative;
import edu.harvard.med.screensaver.model.derivatives.DerivativeScreenResult;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyAction;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.BillingInformation;
import edu.harvard.med.screensaver.model.screens.BillingItem;
import edu.harvard.med.screensaver.model.screens.CherryPick;
import edu.harvard.med.screensaver.model.screens.EquipmentUsed;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.RNAiKnockdownConfirmation;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.Visit;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemType;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

public interface DataAccessPolicy extends AbstractEntityVisitor
{

  public boolean visit(AbaseTestset entity);

  public boolean visit(AttachedFile entity);

  public boolean visit(BillingInformation entity);

  public boolean visit(BillingItem entity);

  public boolean visit(ChecklistItem entity);

  public boolean visit(ChecklistItemType entity);

  public boolean visit(CherryPick entity);

  public boolean visit(Compound entity);

  public boolean visit(Copy entity);

  public boolean visit(CopyAction entity);

  public boolean visit(CopyInfo entity);

  public boolean visit(Derivative entity);

  public boolean visit(DerivativeScreenResult entity);

  public boolean visit(EquipmentUsed entity);

  public boolean visit(Gene entity);

  public boolean visit(LabAffiliation entity);

  public boolean visit(LetterOfSupport entity);

  public boolean visit(Library entity);

  public boolean visit(PlatesUsed entity);

  public boolean visit(Publication entity);

  public boolean visit(ResultValue entity);

  public boolean visit(ResultValueType entity);

  public boolean visit(RNAiKnockdownConfirmation entity);

  public boolean visit(Screen screen);

  public boolean visit(ScreenResult screenResult);

  public boolean visit(ScreensaverUser screensaverUser);

  public boolean visit(SilencingReagent entity);

  public boolean visit(StatusItem entity);

  public boolean visit(Visit visit);

  public boolean visit(Well entity);

}
