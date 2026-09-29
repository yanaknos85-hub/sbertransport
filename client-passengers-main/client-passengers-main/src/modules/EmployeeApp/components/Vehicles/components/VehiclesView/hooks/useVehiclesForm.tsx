/* eslint-disable camelcase, @typescript-eslint/no-explicit-any */
import { Form } from 'antd';
import moment, { Moment } from 'moment';
import {
  useEffect, useState, useMemo, useCallback
} from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import * as routes from 'constants/constants.routes';

import {
  useVehicle, useDeleteVehicle, useAddVehicle, useEditVehicle
} from 'api/vehicles';
import { useUpload, UploadResponse } from 'api/upload';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import useIsMounted from 'shared/hooks/useIsMounted';

import { StoreNames } from 'stores/StoreNames.enum';
import { UUID } from 'utils/io-ts';

import { Ownership, validFormatFile } from '../../../constants/vehicles.constants';
import { FieldNames, FieldsEnum, fieldNames } from '../constants/vehiclesForm.fields';
import { Vehicle } from '../../../types/vehicles.types';
import { VehicleFormValues } from '../types/vehiclesForm.types';

import useVehiclesFormFill from './useVehiclesFormFill';
import { UsedFileFormatEnum } from 'stores/Trip/Trip.interface';
import { MAX_FILE_SIZE_BYTES } from 'shared/constants/validation';

const DEFAULT_FORM_ERROR = 'Необходимо заполнить форму';

const useVehiclesForm = () => {
  const { id } = useRouteMatch<{ id: UUID }>().params;
  const isEdit = !!id;

  const history = useHistory();

  const { [StoreNames.employeeStore]: employeeStore } = useAppStoreContext();
  const { selfEmployee } = employeeStore;
  const {
    organizationId, departmentId, id: employeeId,
  } = selfEmployee;

  const { data } = useVehicle(selfEmployee, id, { enabled: isEdit });
  const [addVehicle] = useAddVehicle(selfEmployee);
  const [editVehicle] = useEditVehicle(selfEmployee);
  const [deleteVehicle] = useDeleteVehicle(selfEmployee);

  const [form] = Form.useForm();
  const isMounted = useIsMounted();

  const { initialValues: fillValues } = useVehiclesFormFill(form);

  const [formError, setFormError] = useState('');

  const goBack = useCallback(() => history.push(routes.PROFILE), [history]);

  const [files, setFiles] = useState<Record<
    FieldsEnum.docs_dl_file |
    FieldsEnum.docs_pts_file |
    FieldsEnum.docs_osago_file |
    FieldsEnum.docs_pdn_file
    , UploadResponse | null
  >>({
    [FieldsEnum.docs_dl_file]: null,
    [FieldsEnum.docs_pts_file]: null,
    [FieldsEnum.docs_osago_file]: null,
    [FieldsEnum.docs_pdn_file]: null,
  });

  const [upload] = useUpload(`/organizations/${organizationId}/departments/${departmentId}/employees/${employeeId}/files/upload`);

  const handleUpload = useCallback(({
    file, onSuccess, onError, field,
  }: { file: any; onSuccess: any; onError: any; field: FieldsEnum }) => {
    const handleInvalidFile = () => {
      setFiles({
        ...files, [field]: {
          fileName: file.name,
          fileSize: file.size,
          fileFormat: file.type,
        },
      });
      onSuccess();
    };

    if (file.size > MAX_FILE_SIZE_BYTES) {
      handleInvalidFile();
      return '';
    }

    const checkedHeifFormat = file.name.split('.').includes(UsedFileFormatEnum.heif);
    if (!validFormatFile.includes(file.type) && !checkedHeifFormat) {
      handleInvalidFile();
      return '';
    }

    return upload({ file })
      .then(res => {
        setFiles({ ...files, [field]: res });
        onSuccess();
      })
      .catch(err => {
        setFiles({ ...files, [field]: null });
        onError({ err });
      });
  }, [upload, files]);

  const initialValues: Partial<VehicleFormValues> = useMemo(
    () => ({
      ...fillValues,
      [FieldsEnum.ownership]: Ownership.USER,
      [FieldsEnum.docs_dl_fio]: selfEmployee.fullName,
    }),
    [fillValues, selfEmployee]
  );

  const [formValues, setFormValues] = useState(initialValues);

  useEffect(() => {
    if (data) {
      setFiles({
        ...files,
        [FieldsEnum.docs_dl_file]: data.documents.driverLic || null,
        [FieldsEnum.docs_pts_file]: data.documents.passportTs || null,
        [FieldsEnum.docs_osago_file]: data.documents.osago || null,
        [FieldsEnum.docs_pdn_file]: data.documents.agreementPdn || null,
      });

      const changedValues = {
        [FieldsEnum.ownership]: data.ownerInfo,

        [FieldsEnum.docs_mc_series]: data.documents.marriageCertificate?.seria,
        [FieldsEnum.docs_mc_number]: data.documents.marriageCertificate?.number,
        [FieldsEnum.docs_mc_dateOfIssue]: moment(data.documents.marriageCertificate?.issueDateDocument),

        [FieldsEnum.vcRegistrationNumber]: data.registrationNumber,
        [FieldsEnum.vcColor]: data.color,
        [FieldsEnum.vcPassengerSeatsCount]: data.passengerSeatsCount,

        [FieldsEnum.docs_dl_fio]: selfEmployee.fullName,
        [FieldsEnum.docs_dl_series]: data.documents.driverLic?.seria,
        [FieldsEnum.docs_dl_number]: data.documents.driverLic?.number,
        [FieldsEnum.docs_dl_issuedBy]: data.documents.driverLic?.issue,
        [FieldsEnum.docs_dl_whereIssued]: data.documents.driverLic?.placeIssue,
        [FieldsEnum.docs_dl_dateOfIssue]: moment(data.documents.driverLic?.issueDateDocument),
        [FieldsEnum.docs_dl_validUntil]: moment(data.documents.driverLic?.finalTimeDocument),
        [FieldsEnum.docs_dl_category]: data.documents.driverLic?.categoria,
        // Поле файла заполняется, но по фатку не используется. Нужно только запуска renderItem
        [FieldsEnum.docs_dl_file]: data.documents.driverLic?.fileName && [
          {
            name: data.documents.driverLic?.fileName,
          },
        ],

        [FieldsEnum.docs_pts_vin]: data.documents.passportTs?.vin,
        [FieldsEnum.docs_pts_brandName]: data.brandName,
        [FieldsEnum.docs_pts_model]: data.model,
        [FieldsEnum.docs_pts_engineVolume]: data.documents.passportTs?.engineVolume || data.engineVolume,
        [FieldsEnum.docs_pts_enginePower]: data.documents.passportTs?.enginePower,
        [FieldsEnum.docs_pts_file]: data.documents.passportTs?.fileName && [
          {
            name: data.documents.passportTs?.fileName,
          },
        ],

        [FieldsEnum.docs_osago_series]: data.insuranceNumber.split(' ')[0],
        [FieldsEnum.docs_osago_number]: data.insuranceNumber.split(' ')[1],
        [FieldsEnum.docs_osago_startTime]: moment(data.documents.osago?.startTimeDocument),
        [FieldsEnum.docs_osago_endTime]: moment(data.documents.osago?.finalTimeDocument),
        [FieldsEnum.docs_osago_file]: data.documents.osago?.fileName && [
          {
            name: data.documents.osago?.fileName,
          },
        ],

        [FieldsEnum.docs_pdn_file]: data.documents.agreementPdn?.fileName && [
          {
            name: data.documents.agreementPdn?.fileName,
          },
        ],

        [FieldsEnum.agreementPersonalData]: data.persDataAccept,
      } as typeof initialValues;

      form.setFieldsValue(changedValues);

      setFormValues({ ...formValues, ...changedValues });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [data]);

  const convertedDate = (value: Moment | undefined) => {
    const createdDate = value?.utc().startOf('day').format();
    return moment(createdDate).add(1, 'd');
  };

  const prepareToSubmit = (values: Partial<VehicleFormValues>) => {
    try {
      const newValues = {
        ...(data?.id && { id: data.id }),

        registrationNumber: values[FieldsEnum.vcRegistrationNumber],
        ownerInfo: values[FieldsEnum.ownership],
        insuranceNumber: `${values[FieldsEnum.docs_osago_series]} ${values[FieldsEnum.docs_osago_number]}`,

        documents: {
          ...(data?.documents || {}),

          ...(values[FieldsEnum.ownership] === Ownership.SPOUSE && {
            marriageCertificate: {
              ...(data?.documents?.marriageCertificate || {}),
              seria: values[FieldsEnum.docs_mc_series],
              number: values[FieldsEnum.docs_mc_number],
              issueDateDocument: convertedDate(values[FieldsEnum.docs_mc_dateOfIssue]),
            },
          }),
          ...((values[FieldsEnum.ownership] === Ownership.SPOUSE
          || values[FieldsEnum.ownership] === Ownership.THIRD_PARTY) && {
            agreementPdn: {
              ...(values.documents_agreementPdn_file && values.documents_agreementPdn_file.length > 0 && files[FieldsEnum.docs_pdn_file]),
              id: data?.documents?.agreementPdn?.id || undefined,
              carId: data?.documents?.agreementPdn?.carId || undefined,
              employeeId: data?.documents?.agreementPdn?.employeeId || undefined,
            },
          }
          ),
          driverLic: {
            ...(values.documents_driverLic_file && values.documents_driverLic_file.length > 0 && files[FieldsEnum.docs_dl_file]),
            id: data?.documents?.driverLic?.id || undefined,
            employeeId: data?.documents?.driverLic?.employeeId || undefined,
            issueDateDocument: convertedDate(values[FieldsEnum.docs_dl_dateOfIssue]),
            finalTimeDocument: convertedDate(values[FieldsEnum.docs_dl_validUntil]),
            seria: values[FieldsEnum.docs_dl_series],
            number: values[FieldsEnum.docs_dl_number],
            issue: values[FieldsEnum.docs_dl_issuedBy],
            placeIssue: values[FieldsEnum.docs_dl_whereIssued],
            categoria: values[FieldsEnum.docs_dl_category],
          },
          passportTs: {
            ...(values.documents_passportTs_file && values.documents_passportTs_file.length > 0 && files[FieldsEnum.docs_pts_file]
            ),
            id: data?.documents?.passportTs?.id || undefined,
            employeeId: data?.documents?.passportTs?.employeeId || undefined,
            carId: data?.documents?.passportTs?.carId || undefined,
            vin: values[FieldsEnum.docs_pts_vin],
            engineVolume: values[FieldsEnum.docs_pts_engineVolume],
            enginePower: values[FieldsEnum.docs_pts_enginePower],
            color: values[FieldsEnum.vcColor],
            passengerSeatsCount: values[FieldsEnum.vcPassengerSeatsCount],
            brandName: values[FieldsEnum.docs_pts_brandName],
            model: values[FieldsEnum.docs_pts_model],
          },
          osago: {
            ...(values.documents_osago_file && values.documents_osago_file.length > 0 && files[FieldsEnum.docs_osago_file]
            ),
            id: data?.documents?.osago?.id || undefined,
            employeeId: data?.documents?.osago?.employeeId || undefined,
            carId: data?.documents?.osago?.carId || undefined,
            seria: values[FieldsEnum.docs_osago_series],
            number: values[FieldsEnum.docs_osago_number],
            startTimeDocument: convertedDate(values[FieldsEnum.docs_osago_startTime]),
            finalTimeDocument: convertedDate(values[FieldsEnum.docs_osago_endTime]),
          },
        },
        // confirmaDataAccuracy: values[FieldsEnum.confirmaDataAccuracy], // Не ожидается беком, но требует разъянений
      } as unknown as Vehicle;

      // eslint-disable-next-line no-console
      // console.log('SAVED VALUES', newValues);

      if (isEdit) {
        editVehicle(newValues).then(() => goBack());
      } else {
        addVehicle(newValues).then(() => goBack());
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };

  const validate = async (nameList: FieldNames[], onSuccess?: (values: VehicleFormValues) => void) => {
    try {
      const values = await form.validateFields(nameList);
      if (!isMounted()) {
        return;
      }

      if (values.errors) {
        setFormError(DEFAULT_FORM_ERROR);
        return;
      }
      setFormError('');

      if (onSuccess) {
        onSuccess(values);
      }
    } catch (err) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const error = err as any;
      setFormError(error.errorFields && error.errorFields.length ? DEFAULT_FORM_ERROR : '');
    }
  };

  const handleChange = (changedValues: VehicleFormValues) => {
    setFormError('');
    setFormValues({ ...formValues, ...changedValues });
  };

  const handleSave = () => {
    validate([...fieldNames], prepareToSubmit);
  };

  const handleDelete = () => deleteVehicle({ vehicleId: id });

  return {
    name: 'vehiclesForm',
    isEdit,
    form,
    error: formError,
    values: formValues,
    initialValues,
    files,
    handleUpload,
    validate,
    handleChange,
    handleSave,
    handleDelete,
    setFiles,
  };
};

export default useVehiclesForm;
