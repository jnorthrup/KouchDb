package kouchdb;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

public interface KvDbAutobeanFactory extends AutoBeanFactory {
AutoBean<ViewResults.Results> viewResults();
AutoBean<ViewResults.Results> viewResults(ViewResults.Results x);
AutoBean<AllDocOptions> allDocs();
AutoBean<AllDocOptions> allDocs(AllDocOptions a);
AutoBean<ReplicateOptions> replicateOptions();
AutoBean<FetchOptions> fetchoptions();
AutoBean<FetchOptions> fetchoptions(FetchOptions options);
}
