import { useForm } from 'antd/lib/form/Form';
import { useContractorDispatcher, useCreateContractor, useUpdateContractor } from 'api/contractors';
import {
  useCallback, useEffect, useMemo, useState
} from 'react';
import { Contractor } from 'stores/Contractors/Contractors.interface';
import { IntegrationTypes, PASSWORD_PROTECTED } from 'constants/constants.app';
import { UUID } from 'utils/io-ts';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { getMaskPhone } from 'utils/getMaskPhone';
import { ignore } from 'utils';
import { ContractorMapped } from '../types';
import { useModal } from '../../../context/modal.context';

export const useModalForm = (contractor: Contractor | undefined) => {
  const [form] = useForm();

  const { modalState, closeModal } = useModal();
  const { logger } = useAppStoreContext();

  // ПОЛЯ ФОРМЫ

  const [isUseDisp, setIsUseDisp] = useState(false);
  const [integrationType, setIntegrationType] = useState<IntegrationTypes | undefined>();
  const [selectedDispatcher, setSelectedDispatcher] = useState<UUID | 'NEW' | undefined>('NEW');

  const { data: dispatcher } = useContractorDispatcher(
    contractor?.id ? (contractor?.id as UUID) : undefined,
    selectedDispatcher === 'NEW' ? undefined : selectedDispatcher,
    {
      suspense: false,
      enabled: !!contractor?.id && !!selectedDispatcher,
    }
  );

  const isPasswordProtected = contractor?.jsonIntegrationParams?.password === PASSWORD_PROTECTED;

  // Обновляем дофолтные значении при обновлении данных об открытом контрагенте
  const initialValues: ContractorMapped = useMemo(
    () => ({
      ...contractor!,
      contractorName: contractor?.integrationParams?.contractorName ?? '',
      contractorRusName: contractor?.integrationParams?.contractorRusName ?? '',
      integrationEmail: contractor?.integrationParams?.integrationEmail ?? '',

      dispatcherFirstName: dispatcher?.firstName ?? '',
      dispatcherLastName: dispatcher?.lastName ?? '',
      dispatcherPatronymic: dispatcher?.patronymic ?? '',
      dispatcherPhone: dispatcher?.phone ?? '',
      dispatcherEmail: dispatcher?.email ?? '',

      APIUrl: contractor?.jsonIntegrationParams?.url ?? '',
      APILogin: contractor?.jsonIntegrationParams?.login ?? '',
      APIPassword: isPasswordProtected ? '' : contractor?.jsonIntegrationParams?.password ?? '',
    }),
    [contractor, isPasswordProtected, dispatcher]
  );

  useEffect(() => {
    form.resetFields();
  }, [initialValues, form.resetFields, modalState.type]);

  useEffect(() => {
    setIsUseDisp(contractor?.integrationType === IntegrationTypes.DISPATCHER);
    setIntegrationType(
      contractor?.integrationType === IntegrationTypes.DISPATCHER
        ? undefined
        : (contractor?.integrationType as IntegrationTypes)
    );
    setSelectedDispatcher(contractor?.mainDispatcher?.id ?? 'NEW');
  }, [contractor, modalState.type]);

  // СОХРАНЕНИЕ ФОРМЫ

  const [createContractor] = useCreateContractor();
  const [editContractor] = useUpdateContractor();

  const saveForm = useCallback(
    (values: any) => {
      const phone = getMaskPhone(values.contactPersonPhone);
      const dispatcherPhone = getMaskPhone(values.dispatcherPhone);

      const isApi = !isUseDisp && integrationType === IntegrationTypes.JSON_API_1_0;
      const isXML
        = !isUseDisp
        && (integrationType === IntegrationTypes.EMAIL_XML_API || integrationType === IntegrationTypes.EMAIL_XML_WOUR_API);

      if (!isUseDisp && !integrationType) {
        logger.toMessage('error', 'Не выбран тип интеграции');
        return;
      }

      const contractorDetails: Contractor = {
        ...initialValues,
        ...values,
        contactPersonPhone: phone,
        integrationParams: isXML
          ? {
            contractorName: values.contractorName,
            contractorRusName: values.contractorRusName,
            integrationEmail: values.integrationEmail,
          }
          : undefined,
        mainDispatcherId: selectedDispatcher === 'NEW' || !isUseDisp ? undefined : selectedDispatcher,
        mainDispatcher: isUseDisp
          ? {
            ...initialValues.mainDispatcher!,
            firstName: values.dispatcherFirstName,
            lastName: values.dispatcherLastName,
            patronymic: values.dispatcherPatronymic,
            phone: dispatcherPhone ?? '',
            email: values.dispatcherEmail,
          }
          : undefined,
        integrationType: isUseDisp ? IntegrationTypes.DISPATCHER : integrationType,
        jsonIntegrationParams: isApi
          ? {
            url: values.APIUrl,
            login: values.APILogin,
            password: isPasswordProtected && !values.APIPassword ? PASSWORD_PROTECTED : values.APIPassword,
          }
          : undefined,
      };

      const saveContractor: (val: Contractor) => Promise<any>
        = modalState.type === 'edit' ? editContractor : createContractor;

      saveContractor(contractorDetails).then(closeModal).catch(ignore);
    },
    [
      isUseDisp,
      integrationType,
      initialValues,
      selectedDispatcher,
      isPasswordProtected,
      modalState.type,
      editContractor,
      createContractor,
      closeModal,
      logger,
    ]
  );

  return {
    form,
    saveForm,
    initialValues,
    isUseDisp,
    setIsUseDisp,
    integrationType,
    setIntegrationType,
    selectedDispatcher,
    setSelectedDispatcher,
    isPasswordProtected,
  };
};
