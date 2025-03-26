import al.pattyjog.mapjams.MapJamsDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

val androidModule: Module = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = MapJamsDatabase.Schema,
            context = get(),
            name = "mapjams.db"
        )
    }
}