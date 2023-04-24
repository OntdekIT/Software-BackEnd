package Ontdekstation013.ClimateChecker.Mocks;

import Ontdekstation013.ClimateChecker.models.Sensor;
import Ontdekstation013.ClimateChecker.models.Station;
import Ontdekstation013.ClimateChecker.repositories.StationRepository;
import Ontdekstation013.ClimateChecker.repositories.StationRepositoryCustom;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MockStationRepo implements StationRepositoryCustom {
    public List<Station> stations;

    public MockStationRepo() {
        this.stations = new ArrayList<>();
    }

    public void FillDataBase(List<Station> stations) {
        this.stations = stations;
    }

    @Override
    public List<Station> findAll() {
        return stations;
    }

    @Override
    public List<Station> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Station> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<Station> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return stations.size();
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(Station entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends Station> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Station> S save(S entity) {
        stations.add(entity);
        return null;
    }

    @Override
    public <S extends Station> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Station> findById(Long aLong) {

        for (Station station: stations
        ) {

            if (station.getStationID() == aLong){
                return Optional.of(station);
            }

        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Station> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Station> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<Station> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Station getOne(Long aLong) {
        return null;
    }

    @Override
    public Station getById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Station> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Station> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Station> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Station> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Station> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Station> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Station, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }


    @Override
    public List<Station> findAllByOwner_UserID(long userId) {
        List<Station> stationList = new ArrayList<>();
        for (Station station: stations
        ) {
            if (station.getOwner().getUserID() == userId){
                stationList.add(station);
            }
        }
        return stationList;
    }

    @Override
    public Optional<Station> findByRegistrationCodeAndDatabaseTag(long registrationCode, String databaseTag) {
        return null;
    }
}
