package Ontdekstation013.ClimateChecker.Mocks;

import Ontdekstation013.ClimateChecker.models.Sensor;
import Ontdekstation013.ClimateChecker.models.SensorType;
import Ontdekstation013.ClimateChecker.repositories.SensorRepository;
import Ontdekstation013.ClimateChecker.repositories.SensorRepositoryCustom;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MockSensorRepo implements SensorRepositoryCustom {
    public List<Sensor> sensors;


    public MockSensorRepo(){
        this.sensors = new ArrayList<>();
    }

    public void FillDatabase(List<Sensor> sensors){
        this.sensors = sensors;
    }

    @Override
    public List<Sensor> findAll() {
        return sensors;
    }

    @Override
    public List<Sensor> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Sensor> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<Sensor> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return sensors.size();
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(Sensor entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends Sensor> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Sensor> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Sensor> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Sensor> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Sensor> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<Sensor> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Sensor getOne(Long aLong) {
        return null;
    }

    @Override
    public Sensor getById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Sensor> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Sensor> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Sensor> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Sensor> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Sensor> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Sensor> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Sensor, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public Optional<Sensor> findById(Long aLong) {

        for (Sensor sensor: sensors
        ) {

            if (sensor.getSensorID() == aLong){
                return Optional.of(sensor);
            }

        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }


    @Override
    public List<Sensor> findAllBySensorType_TypeID(long typeId) {
        List<Sensor> sensorList = new ArrayList<>();

        for (Sensor sensor: sensors
        ) {

            if (sensor.getSensorType().getTypeID() == typeId){
                sensorList.add(sensor);
            }

        }
        return sensorList;
    }


    @Override
    public List<Sensor> findByStation_StationID(long stationId) {
        List<Sensor> sensorList = new ArrayList<>();

        for (Sensor sensor: sensors
        ) {

            if (sensor.getStation().getStationID() == stationId){
                sensorList.add(sensor);
            }

        }
        return sensorList;
    }

    @Override
    public Optional<List<Sensor>> findAllByActiveData(boolean activeDataInput) {
        return Optional.empty();
    }
}
