package kouchdb;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import kouchdb.command.BatchFetch;
import kouchdb.command.Fetchdoc;
import kouchdb.command.Replication;

public interface KvDbAutobeanFactory extends AutoBeanFactory {
AutoBean<ViewResults.Results> viewResults();
AutoBean<ViewResults.Results> viewResults(ViewResults.Results x);
AutoBean<BatchFetch.AllDocOptions> allDocs();
AutoBean<BatchFetch.AllDocOptions> allDocs(BatchFetch.AllDocOptions a);
AutoBean<Replication.ReplicateOptions> replicateOptions();
AutoBean<Fetchdoc.FetchOptions> fetchoptions();
AutoBean<Fetchdoc.FetchOptions> fetchoptions(Fetchdoc.FetchOptions options);
}
