package kouchdb;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import kouchdb.command.Commands;

public interface KvDbAutobeanFactory extends AutoBeanFactory {
AutoBean<ViewResults.Results> viewResults();
AutoBean<ViewResults.Results> viewResults(ViewResults.Results x);
AutoBean<Commands.BatchFetch.AllDocOptions> allDocs();
AutoBean<Commands.BatchFetch.AllDocOptions> allDocs(Commands.BatchFetch.AllDocOptions a);
AutoBean<Commands.Sync.ReplicateOptions> replicateOptions();
AutoBean<Commands.Fetchdoc.FetchOptions> fetchoptions();
AutoBean<Commands.Fetchdoc.FetchOptions> fetchoptions(Commands.Fetchdoc.FetchOptions options);
}
