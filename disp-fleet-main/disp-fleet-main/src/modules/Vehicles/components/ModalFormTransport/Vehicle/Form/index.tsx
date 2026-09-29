import React, {
  useEffect, useState, useMemo, FC
} from 'react';
import { Select } from '@sber-sbertransport/ui-kit/src';
import classnames from 'classnames';
import {
  Row, Col, Form, Spin, InputNumber
} from 'antd';
import type { Store } from 'antd/lib/form/interface';
import { useTranslation } from 'i18n';

import { useVehicleDirectorySearch } from 'api/directories/directories.api';
import { VehicleListContentItem, VehicleListSearchRequest } from 'api/directories/directories.types';
import { Button } from 'components/Button';
import { DirectoriesTypes } from 'modules/Vehicles/constants/transport';
import { useBrandsSelectOptions } from 'modules/Vehicles/hooks/useBrandsSelectOptions';
import { useModelsSelectOptions } from 'modules/Vehicles/hooks/useModelsSelectOptions';
import { TSelectOption } from 'types/vehicles';
import VehicleList from '../List';

import styles from './styles.module.scss';

export interface IFormData {
  brandId?: string;
  modelId?: string;
  engineTypeId?: string;
  driveId?: string;
  vehicleId: string;
  transmissionTypeId?: string;
  bodyTypeId?: string;
  manufacturePeriod?: string;
  year?: string;
}

interface IProps {
  initialValues: IFormData | undefined;
  onFinish: (data: IFormData) => void;
}

const VehicleForm: FC<IProps> = ({ initialValues, onFinish }) => {
  const [form] = Form.useForm();

  const {
    t: {
      global: general,
      Transport: {
        directory: { types, global },
      },
    },
  } = useTranslation();

  const vehicles = types[DirectoriesTypes.Vehicles];

  const [brandId, setBrandId] = useState<string | undefined>(initialValues?.brandId);
  const [modelId, setModelId] = useState<string | undefined>(initialValues?.modelId);
  const [engineTypeId, setEngineTypeId] = useState<string | undefined>(initialValues?.engineTypeId);
  const [driveId, setDriveId] = useState<string | undefined>(initialValues?.driveId);
  const [vehicleId, setVehicleId] = useState<string | undefined>(initialValues?.vehicleId);
  const [bodyTypeId, setBodyTypeId] = useState<string | undefined>(initialValues?.bodyTypeId);
  const [transmissionTypeId, setTransmissionTypeId] = useState<string | undefined>(initialValues?.transmissionTypeId);
  const [year, setYear] = useState<string | undefined>(initialValues?.year);
  const [manufacturePeriod, setManufacturePeriod] = useState<string | undefined>(initialValues?.manufacturePeriod);
  const [enginePower, setEnginePower] = useState<number | undefined>();
  const [engineVolume, setEngineVolume] = useState<number | undefined>();
  const [vehicleList, setVehicleList] = useState<VehicleListContentItem[]>([]);

  const [bodyTypeOptions, setBodyTypeOptions] = useState<TSelectOption[]>([]);
  const [engineTypeOptions, setEngineTypeOptions] = useState<TSelectOption[]>([]);
  const [wheelDriveOptions, setWheelDriveOptions] = useState<TSelectOption[]>([]);
  const [transmissionTypeOptions, setTransmissionTypeOptions] = useState<TSelectOption[]>([]);
  const [manufacturePeriodOptions, setManufacturePeriodOptions] = useState<TSelectOption[]>([]);
  const [enginePowerOptions, setEnginePowerOptions] = useState<TSelectOption[]>([]);
  const [engineVolumeOptions, setEngineVolumeOptions] = useState<TSelectOption[]>([]);

  const isListShowed = useMemo(() => !!brandId && !!modelId, [brandId, modelId]);

  const {
    options: brandsOptions, isLoading: isBrandsLoading, onSearch: onBrandsSearch,
  } = useBrandsSelectOptions();

  const handleSelectBrandId = value => {
    if (brandId !== value) {
      setBrandId(value);
      setVehicleList([]);
      setModelId(undefined);
    }
  };

  const {
    options: modelsOptions,
    isLoading: isModelsLoading,
    onSearch: onModelsSearch,
  } = useModelsSelectOptions(brandId);

  const handleYearChange = (value: number | null) => {
    setYear(value?.toString());
  };

  const {
    isLoading: isVehiclesListLoading,
    refetch,
  } = useVehicleDirectorySearch({
    vehicle: {
      brand: brandId,
      model: modelId,
      bodyType: bodyTypeId,
      manufactureYear: year,
      manufacturePeriod,
    },
    engine: {
      engineType: engineTypeId,
      drive: driveId,
      transmissionType: transmissionTypeId,
      engineCapacity: engineVolume,
      enginePower,
    },
    pageSetting: { page: 0, size: 100 },
  } as unknown as typeof VehicleListSearchRequest);

  useEffect(() => {
    if (brandId && modelId) {
      refetch().then(data => {
        setVehicleList((data?.content || []) as VehicleListContentItem[]);
        const filteredSelectOptions = data?.filters;

        if (filteredSelectOptions?.bodyType && filteredSelectOptions?.bodyType.length >= 0) {
          setBodyTypeOptions(filteredSelectOptions?.bodyType.map(({ id, title }) => ({ value: id, label: title })));
        }
        if (filteredSelectOptions?.transmissionType && filteredSelectOptions?.transmissionType.length >= 0) {
          setTransmissionTypeOptions(
            filteredSelectOptions?.transmissionType.map(({ id, title }) => ({ value: id, label: title }))
          );
        }
        if (filteredSelectOptions?.manufacturePeriod && filteredSelectOptions?.manufacturePeriod.length >= 0) {
          setManufacturePeriodOptions(
            filteredSelectOptions?.manufacturePeriod.map(value => ({ value: value, label: value }))
          );
        }
        if (filteredSelectOptions?.engineType && filteredSelectOptions?.engineType.length >= 0) {
          setEngineTypeOptions(filteredSelectOptions?.engineType.map(({ id, title }) => ({ value: id, label: title })));
        }
        if (filteredSelectOptions?.driveType && filteredSelectOptions?.driveType.length >= 0) {
          setWheelDriveOptions(filteredSelectOptions?.driveType.map(({ id, title }) => ({ value: id, label: title })));
        }
        if (filteredSelectOptions?.engineCapacity && filteredSelectOptions?.engineCapacity.length >= 0) {
          setEngineVolumeOptions(
            filteredSelectOptions?.engineCapacity.map(value => ({ value: value, label: String(value) }))
          );
        }
        if (filteredSelectOptions?.enginePower && filteredSelectOptions?.enginePower.length >= 0) {
          setEnginePowerOptions(
            filteredSelectOptions?.enginePower.map(value => ({ value: value, label: String(value) }))
          );
        }
      });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    brandId,
    modelId,
    engineTypeId,
    driveId,
    bodyTypeId,
    transmissionTypeId,
    year,
    manufacturePeriod,
    enginePower,
    engineVolume,
  ]);

  const handleFinish = () => {
    if (vehicleId) {
      onFinish({
        brandId, modelId, engineTypeId, driveId, vehicleId, year,
      });
    }
  };

  const resetModelId = () => {
    setModelId(undefined);
    setYear(undefined);
    setBodyTypeId(undefined);
    setEngineTypeId(undefined);
    setEnginePower(undefined);
    setEngineVolume(undefined);
    setDriveId(undefined);
    setTransmissionTypeId(undefined);
    setManufacturePeriod(undefined);
    form.resetFields([
      'modelId',
      'year',
      'bodyTypeId',
      'manufacturePeriod',
      'transmissionTypeId',
      'engineTypeId',
      'enginePower',
      'engineVolume',
      'driveId',
    ]);
    setVehicleList([]);
  };

  const resetBrandId = () => {
    resetModelId();
    setBrandId(undefined);
    onBrandsSearch('');
    form.resetFields(['brandId']);
  };

  const getPlaceholderSubject = (placeholder: string) => (modelId ? placeholder : vehicles.objective.fields.model);

  return (
    <Form
      form={form}
      initialValues={initialValues as Store}
      className={styles.form}
    >
      <Row>
        <Col>
          <Form.Item
            name="brandId"
            label={vehicles.fields.brand}
            className={styles.formItem}
          >
            <Select
              allowClear
              showSearch
              showDivider
              size="middle"
              filterOption={false}
              placeholder={`${global.select} ${vehicles.objective.fields.brand}`}
              options={!isBrandsLoading ? brandsOptions : []}
              notFoundContent={isBrandsLoading ? <Spin size="small" /> : null}
              onSearch={onBrandsSearch}
              onSelect={handleSelectBrandId}
              onClear={resetBrandId}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="modelId"
            label={vehicles.fields.model}
            className={styles.formItem}
          >
            <Select
              allowClear
              showSearch
              showDivider
              size="middle"
              disabled={!brandId}
              filterOption={false}
              placeholder={`${global.select} ${
                brandId ? vehicles.objective.fields.model : vehicles.objective.fields.brand
              }`}
              options={!isModelsLoading ? modelsOptions : []}
              notFoundContent={isModelsLoading ? <Spin size="small" /> : null}
              onSearch={onModelsSearch}
              onSelect={value => setModelId(value?.toString())}
              onClear={resetModelId}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="year"
            label={global.manufactureYear}
            className={styles.formItem}
            rules={[
              { required: true },
              {
                type: 'number',
                min: 1950,
                max: new Date().getFullYear(),
                message: global.manufactureYearErrorMessage({
                  minYear: 1950,
                  maxYear: new Date().getFullYear(),
                }),
              },
            ]}
          >
            <InputNumber
              type="number"
              disabled={!modelId}
              className={styles.input}
              placeholder={`${global.input} ${getPlaceholderSubject(general.year.toLowerCase())}`}
              min={1950}
              max={new Date().getFullYear()}
              onChange={handleYearChange}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="manufacturePeriod"
            label={vehicles.fields.manufacturePeriod}
            className={styles.formItem}
          >
            <Select
              allowClear
              showDivider
              size="middle"
              disabled={!modelId || !year}
              placeholder={`${global.select} ${
                year ? vehicles.objective.fields.manufacturePeriod : global.manufactureYear
              }`}
              options={!isVehiclesListLoading ? manufacturePeriodOptions : []}
              notFoundContent={isVehiclesListLoading ? <Spin size="small" /> : null}
              onSelect={value => setManufacturePeriod(value?.toString())}
              onClear={() => setManufacturePeriod(undefined)}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="bodyTypeId"
            label={vehicles.fields.bodyType}
            className={styles.formItem}
          >
            <Select
              allowClear
              showSearch
              showDivider
              disabled={!modelId}
              filterOption={true}
              optionFilterProp="label"
              size="middle"
              placeholder={`${global.select} ${getPlaceholderSubject(vehicles.objective.fields.bodyType)}`}
              options={!isVehiclesListLoading ? bodyTypeOptions : []}
              notFoundContent={isVehiclesListLoading ? <Spin size="small" /> : null}
              onSelect={value => setBodyTypeId(value?.toString())}
              onClear={() => setBodyTypeId(undefined)}
            />
          </Form.Item>
        </Col>
      </Row>

      <Row>
        <Col>
          <Form.Item
            name="engineTypeId"
            label={vehicles.fields.engineType}
            className={styles.formItem}
          >
            <Select
              allowClear
              showDivider
              disabled={!modelId}
              size="middle"
              placeholder={`${global.select} ${getPlaceholderSubject(vehicles.objective.fields.engineType)}`}
              options={!isVehiclesListLoading ? engineTypeOptions : []}
              notFoundContent={isVehiclesListLoading ? <Spin size="small" /> : null}
              onSelect={value => setEngineTypeId(value?.toString())}
              onClear={() => setEngineTypeId(undefined)}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="engineVolume"
            label={vehicles.fields.engineVolume}
            className={styles.formItem}
          >
            <Select
              allowClear
              showDivider
              disabled={!modelId}
              size="middle"
              placeholder={`${global.select} ${getPlaceholderSubject(vehicles.objective.fields.engineVolume)}`}
              options={!isVehiclesListLoading ? engineVolumeOptions : []}
              notFoundContent={isVehiclesListLoading ? <Spin size="small" /> : null}
              onSelect={value => setEngineVolume(Number(value))}
              onClear={() => setEngineVolume(undefined)}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="enginePower"
            label={vehicles.fields.power}
            className={styles.formItem}
          >
            <Select
              allowClear
              showDivider
              size="middle"
              disabled={!modelId || !engineVolume}
              placeholder={`${global.select} ${
                engineVolume ? vehicles.objective.fields.power : vehicles.objective.fields.engineVolume
              }`}
              options={!isVehiclesListLoading ? enginePowerOptions : []}
              notFoundContent={isVehiclesListLoading ? <Spin size="small" /> : null}
              onSelect={value => setEnginePower(Number(value))}
              onClear={() => setEnginePower(undefined)}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="transmissionTypeId"
            label={vehicles.fields.transmissionType}
            className={styles.formItem}
          >
            <Select
              allowClear
              showDivider
              size="middle"
              disabled={!modelId}
              placeholder={`${global.select} ${getPlaceholderSubject(vehicles.objective.fields.transmissionType)}`}
              options={!isVehiclesListLoading ? transmissionTypeOptions : []}
              notFoundContent={isVehiclesListLoading ? <Spin size="small" /> : null}
              onSelect={value => setTransmissionTypeId(value?.toString())}
              onClear={() => setTransmissionTypeId(undefined)}
            />
          </Form.Item>
        </Col>

        <Col>
          <Form.Item
            name="driveId"
            label={vehicles.fields.wheelDrive}
            className={styles.formItem}
          >
            <Select
              allowClear
              showDivider
              size="middle"
              disabled={!modelId}
              placeholder={`${global.select} ${getPlaceholderSubject(vehicles.objective.fields.wheelDrive)}`}
              options={!isVehiclesListLoading ? wheelDriveOptions : []}
              notFoundContent={isVehiclesListLoading ? <Spin size="small" /> : null}
              onSelect={value => setDriveId(value?.toString())}
              onClear={() => setDriveId(undefined)}
            />
          </Form.Item>
        </Col>
      </Row>

      {isListShowed && (
        <VehicleList
          vehicle={vehicleId}
          vehicleList={vehicleList}
          selectVehicle={setVehicleId}
          isLoading={isVehiclesListLoading}
        />
      )}

      <hr className={classnames(styles.separator, !isListShowed && styles.separator_hidden)} />

      <Button
        type="primary"
        disabled={!vehicleId || !year}
        className={styles.button}
        onClick={handleFinish}
      >
        {general.stepForward}
      </Button>
    </Form>
  );
};

export default VehicleForm;
