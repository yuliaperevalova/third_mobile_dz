package com.example.third_dz.di

import android.content.Context
import androidx.room.Room
import com.example.third_dz.data.api.GhibliFilmsApi
import com.example.third_dz.data.local.AppDatabase
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.FilmDao
import com.example.third_dz.data.local.CollectionDao
import com.example.third_dz.data.local.UserFilmRecordDao
import com.example.third_dz.data.local.migrations.Migrations
import com.example.third_dz.data.repository.GhibliFilmsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://ghibliapi.vercel.app/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val headerInterceptor = Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .header("User-Agent", "GhibliFilmsApp/1.0")
                .header("Accept", "application/json")
                .build()
            chain.proceed(newRequest)
        }
        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGhibliFilmsApi(retrofit: Retrofit): GhibliFilmsApi {
        return retrofit.create(GhibliFilmsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "ghibli_db")
            .addMigrations(*Migrations.ALL)
            .build()
    }

    @Provides
    @Singleton
    fun provideFavouriteFilmDao(db: AppDatabase): FavouriteFilmDao {
        return db.favouriteFilmDao()
    }

    @Provides
    @Singleton
    fun provideFilmDao(db: AppDatabase): FilmDao {
        return db.filmDao()
    }

    @Provides
    @Singleton
    fun provideUserFilmRecordDao(db: AppDatabase): UserFilmRecordDao {
        return db.userFilmRecordDao()
    }

    @Provides
    @Singleton
    fun provideCollectionDao(db: AppDatabase): CollectionDao {
        return db.collectionDao()
    }

    @Provides
    @Singleton
    fun provideGhibliFilmsRepository(api: GhibliFilmsApi, filmDao: FilmDao): GhibliFilmsRepository {
        return GhibliFilmsRepository(api, filmDao)
    }

    @Provides
    @Singleton
    fun providePeopleDao(db: AppDatabase) = db.peopleDao()

    @Provides
    @Singleton
    fun provideLocationsDao(db: AppDatabase) = db.locationsDao()

    @Provides
    @Singleton
    fun provideSpeciesDao(db: AppDatabase) = db.speciesDao()

    @Provides
    @Singleton
    fun provideVehiclesDao(db: AppDatabase) = db.vehiclesDao()

    @Provides
    @Singleton
    fun providePinnedEntityDao(db: AppDatabase) = db.pinnedEntityDao()

    @Provides
    @Singleton
    fun provideUniverseRepository(
        api: GhibliFilmsApi,
        db: AppDatabase,
        peopleDao: com.example.third_dz.data.local.PeopleDao,
        locationsDao: com.example.third_dz.data.local.LocationsDao,
        speciesDao: com.example.third_dz.data.local.SpeciesDao,
        vehiclesDao: com.example.third_dz.data.local.VehiclesDao
    ) = com.example.third_dz.data.repository.UniverseRepository(api, db, peopleDao, locationsDao, speciesDao, vehiclesDao)

    @Provides
    @Singleton
    fun providePinnedRepository(dao: com.example.third_dz.data.local.PinnedEntityDao) =
        com.example.third_dz.data.repository.PinnedRepository(dao)
}
