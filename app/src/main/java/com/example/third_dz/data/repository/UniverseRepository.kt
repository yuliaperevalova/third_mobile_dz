package com.example.third_dz.data.repository

import androidx.room.withTransaction
import com.example.third_dz.data.api.GhibliFilmsApi
import com.example.third_dz.data.local.AppDatabase
import com.example.third_dz.data.local.FilmLocationCrossRef
import com.example.third_dz.data.local.FilmPersonCrossRef
import com.example.third_dz.data.local.FilmSpeciesCrossRef
import com.example.third_dz.data.local.FilmVehicleCrossRef
import com.example.third_dz.data.local.LocationsDao
import com.example.third_dz.data.local.PeopleDao
import com.example.third_dz.data.local.SpeciesDao
import com.example.third_dz.data.local.VehiclesDao
import com.example.third_dz.data.local.toEntity
import com.example.third_dz.data.local.toLocation
import com.example.third_dz.data.local.toPerson
import com.example.third_dz.data.local.toSpecies
import com.example.third_dz.data.local.toVehicle
import com.example.third_dz.data.model.Location
import com.example.third_dz.data.model.Person
import com.example.third_dz.data.model.Species
import com.example.third_dz.data.model.Vehicle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UniverseRepository @Inject constructor(
    private val api: GhibliFilmsApi,
    private val db: AppDatabase,
    private val peopleDao: PeopleDao,
    private val locationsDao: LocationsDao,
    private val speciesDao: SpeciesDao,
    private val vehiclesDao: VehiclesDao
) {

    suspend fun refreshAll() {
        val people = api.getAllPeople()
        val locations = api.getAllLocations()
        val species = api.getAllSpecies()
        val vehicles = api.getAllVehicles()

        db.withTransaction {
            peopleDao.insertAll(people.map { it.toEntity() })
            locationsDao.insertAll(locations.map { it.toEntity() })
            speciesDao.insertAll(species.map { it.toEntity() })
            vehiclesDao.insertAll(vehicles.map { it.toEntity() })

            // Extract cross-refs from URL lists
            val personCrossRefs = people.flatMap { person ->
                person.films.mapNotNull { url -> extractId(url)?.let { FilmPersonCrossRef(it, person.id) } }
            }
            val locationCrossRefs = locations.flatMap { location ->
                location.films.mapNotNull { url -> extractId(url)?.let { FilmLocationCrossRef(it, location.id) } }
            }
            val speciesCrossRefs = species.flatMap { sp ->
                sp.films.mapNotNull { url -> extractId(url)?.let { FilmSpeciesCrossRef(it, sp.id) } }
            }
            val vehicleCrossRefs = vehicles.flatMap { vehicle ->
                vehicle.films.mapNotNull { url -> extractId(url)?.let { FilmVehicleCrossRef(it, vehicle.id) } }
            }

            // Insert cross-refs (DAOs need insert methods - will be added)
            // For now, this is placeholder - actual insert will be in DAO
        }
    }

    fun observeAllPeople(): Flow<List<Person>> =
        peopleDao.observeAll().map { list -> list.map { it.toPerson() } }

    fun observePersonById(id: String): Flow<Person?> =
        peopleDao.observeById(id).map { it?.toPerson() }

    fun observeAllLocations(): Flow<List<Location>> =
        locationsDao.observeAll().map { list -> list.map { it.toLocation() } }

    fun observeLocationById(id: String): Flow<Location?> =
        locationsDao.observeById(id).map { it?.toLocation() }

    fun observeAllSpecies(): Flow<List<Species>> =
        speciesDao.observeAll().map { list -> list.map { it.toSpecies() } }

    fun observeAllVehicles(): Flow<List<Vehicle>> =
        vehiclesDao.observeAll().map { list -> list.map { it.toVehicle() } }

    fun observeSpeciesById(id: String): Flow<Species?> =
        speciesDao.observeById(id).map { it?.toSpecies() }

    fun observeVehicleById(id: String): Flow<Vehicle?> =
        vehiclesDao.observeById(id).map { it?.toVehicle() }

    private fun extractId(url: String): String? =
        url.substringAfterLast('/').takeIf { it.isNotBlank() }
}
