package kouchdb.command;

import kouchdb.ann.ProtoNumber;
import kouchdb.ann.ProtoOrigin;


@ProtoOrigin("kouchdb.command.DbInfo")
public interface DbInfo {

	@ProtoNumber(1)
	String	getDb();

}
