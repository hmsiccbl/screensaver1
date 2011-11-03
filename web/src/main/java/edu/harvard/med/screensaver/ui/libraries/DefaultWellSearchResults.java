package edu.harvard.med.screensaver.ui.libraries;

import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.LibraryContentsVersionReference;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;
import edu.harvard.med.screensaver.ui.arch.datatable.model.VirtualPagingEntityDataModel;

public class DefaultWellSearchResults extends WellSearchResults
{
  private GenericEntityDAO _dao;

  protected DefaultWellSearchResults()
  {
    super();
  }

  public DefaultWellSearchResults(GenericEntityDAO dao,
                                  LibrariesDAO librariesDao,
                                  EntityViewPolicy entityViewPolicy,
                                  LibraryViewer libraryViewer,
                                  WellViewer wellViewer,
                                  StructureImageProvider structureImageProvider,
                                  LibraryContentsVersionReference libraryContentsVersionRef,
                                  List<DataExporter<Tuple<String>>> dataExporters)
  {
    super(dao, librariesDao, entityViewPolicy, libraryViewer, wellViewer, structureImageProvider, libraryContentsVersionRef, dataExporters);
    _dao = dao;
  }

  @Override
  protected void initialize(DataFetcher<Tuple<String>,String,PropertyPath<Well>> dataFetcher)
  {
    DataTableModel<Tuple<String>> dataModel =
      new VirtualPagingEntityDataModel<String,Well,Tuple<String>>(dataFetcher, new RowsToFetchReference());
    initialize(dataModel);
  }

}
