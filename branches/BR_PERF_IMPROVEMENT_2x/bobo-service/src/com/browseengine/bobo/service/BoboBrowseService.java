package com.browseengine.bobo.service;

import com.browseengine.bobo.api.BrowseException;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;

public interface BoboBrowseService {
	BrowseResult browse(BrowseRequest req) throws BrowseException;
}
