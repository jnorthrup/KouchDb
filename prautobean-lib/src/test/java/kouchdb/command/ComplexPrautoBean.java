package kouchdb.command
        ;

import kouchdb.ann.Optional;
import kouchdb.ann.ProtoNumber;
import kouchdb.ann.ProtoOrigin;

import java.util.List;


@ProtoOrigin("kouchdb.command.CreateDb.ComplexPrautoBean")
public interface ComplexPrautoBean {

    @Optional(value = 1)
    @ProtoNumber(value = 2)
    boolean getAutoCompaction();


    @Optional(2)
    @ProtoNumber(1)
    String getName();


    @Optional(3)
    @ProtoNumber(3)
    String getCache();


    @Optional(4)
    @ProtoNumber(4)
    String getAdapter();

    @Optional(5)
    @ProtoNumber(5)
    List<ComplexPrautoBean> getComplexObject();

    @Optional(6)
    @ProtoNumber(6)
    List<String> getStringList();

    @Optional(7)
    @ProtoNumber(7)
    List< TheEnum > getEnumList();

}
