# WorkManager looks this class up by name. Keep the worker constructor.
-keep class be.scri.helpers.dataupdate.DataUpdateCheckWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
