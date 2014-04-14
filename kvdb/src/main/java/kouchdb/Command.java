package kouchdb;



public enum Command{
    CreateDb ,
    DeleteDb,
    PersistDoc,
    Fetchdoc,
    DeleteDoc,
    BatchCreate,
    FetchBatch,
    Changes,
    Replication,
    Sync,
    SaveAttachment,
    GetAttachment,
    DeleteAttachment,
    QueryDb,
    DbInfo,
    Compaction,
    RevisionDiff,
    Events,
    Plugins,;

    public static void main(String[] args) {
        System.out.println("public interface Commands{" );
        for (Command command : Command.values()) {
            System.out.println("  interface "+command.name()+"{}");
        }
        System.out.println("}");
    }
}