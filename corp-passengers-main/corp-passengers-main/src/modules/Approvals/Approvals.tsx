import React, { FC, useEffect, useState } from 'react';
import { Popconfirm, Table } from 'antd';
import Form from 'antd/lib/form';
import { useApprovalSettings } from 'api/approval-settings';
import { useGeoZones } from 'api/geo-zones';
import { useProfile } from 'api/profile';
import { useAllTripPurposes } from 'api/purposes';
import { useTranslation } from 'i18n';
import { ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { UUID } from 'utils/io-ts';
import uniqId from 'utils/uid';
import SwitchField from 'shared/models/ModelDetail/FieldTypes/SwitchField';
import { Button } from 'shared/components/Button/Button';
import { Icon } from 'shared/components/Icon';
import { SaveOutlined } from '@ant-design/icons';
import { Footer } from './components/Footer';
import { useColumns } from './hooks/useColumns';
import { useTableForm } from './hooks/useTableForm';
import { ApprovalsRecord } from './types/types';
import { getEmptyData, mapSettingsToRecords } from './utils/utils';
import styles from './Approvals.module.scss';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';

const Approvals: FC<{ selectedTransportType: TransportTypes }> = ({ selectedTransportType }) => {
  const { t } = useTranslation();

  const { data: profile } = useProfile();
  // @ts-ignore
  const { data: purposes } = useAllTripPurposes(profile.organizationId);
  const { data: geoZones } = useGeoZones();

  // @ts-ignore
  const {
    refetch,
    data: approvalSettings,
  } = useApprovalSettings(profile.organizationId, selectedTransportType);

  const [isSettingsRefetching, setIsSettingsRefetching] = useState(false);

  const [state, setState] = useState<ApprovalsRecord[]>(
    mapSettingsToRecords(geoZones, approvalSettings?.approvalSettings)
  );

  const [approvalId, setApprovalId] = useState<UUID>();
  const [isRequired, setIsRequired] = useState<boolean>((state.length && state[0].approvalActive) || false);

  const [isAddSettings, setAddSettings] = useState(false);
  const [rebootSettings, setRebootSettings] = useState(false);

  const {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    form, onFinish, onCancel, onDefault, setFormValues,
  } = useTableForm(
    // @ts-ignore
    profile,
    purposes.byId,
    geoZones,
    selectedTransportType,
    state,
    setState,
    approvalId,
    setRebootSettings
  );

  useEffect(() => {
    setIsSettingsRefetching(true);
    setTimeout(() => {
      refetch().then(settings => {
        setApprovalId(settings?.approvalSettings.id);
        setState(mapSettingsToRecords(geoZones, settings?.approvalSettings));

        if (settings?.approvalSettings.approvalActive !== undefined) {
          setIsRequired(settings.approvalSettings.approvalActive);
          setFormValues({
            approvalDocumentCheck: settings.approvalSettings?.approvalDocumentCheck,
            affirmativeActive: settings.approvalSettings?.affirmativeActive,
            tripConfirmationActive: settings.approvalSettings?.tripConfirmationActive,
            tripConfirmationDocumentCheck: settings.approvalSettings?.tripConfirmationDocumentCheck,
            tripApprovalActive: settings.approvalSettings?.tripApprovalActive,
          });
        }
      }).finally(() => {
        setIsSettingsRefetching(false);
      });
    }
    , 2000);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [geoZones, refetch, selectedTransportType, rebootSettings]);

  useEffect(() => {
    form.setFieldsValue({ minimalSum: state[0]?.minimalSum });
  }, [form, state]);

  useEffect(() => {
    form.setFieldsValue({ type: selectedTransportType });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onToggleAddSettings = () => setAddSettings(!isAddSettings);

  const addRow = () => {
    state.length
      ? setState([...state, getEmptyData(uniqId() as UUID, selectedTransportType)])
      : setState([getEmptyData(uniqId() as UUID, selectedTransportType)]);
  };

  const deleteRow = (id: UUID) => {
    setState(state.filter(item => item.rowId !== id));
  };

  const columns = useColumns(
    purposes.purposes.map(p => ({ label: p.label, value: p.id })),
    geoZones.map(g => ({ label: g.name, value: g.id })),
    state,
    setState,
    deleteRow
  );

  const onChange = (e: boolean): void => {
    setIsRequired(e);
    onToggleAddSettings();
  };

  const yesNoOptions = [
    { value: 'true', label: 'да' },
    { value: 'false', label: 'нет' },
  ];

  return isSettingsRefetching ? <SpinWrapped /> : (
    <div className={styles.model_detail_page}>
      <Form
        layout="horizontal"
        form={form}
        size="middle"
        initialValues={{
          type: selectedTransportType,
        }}
      >
        <div>
          <div>
            {selectedTransportType === TransportTypes.PUBLIC && (
            <>
              <Form.Item name="approvalDocumentCheck" initialValue={isRequired}>
                <SwitchField
                  editable
                  label={'Необходимость проверки документа на этапе "Создание"'}
                  name="approvalDocumentCheck"
                  fieldType={ModelFormFieldType.SWITCH}
                  options={yesNoOptions}
                  initialValue={state[0]?.approvalDocumentCheck}
                />
              </Form.Item>

              <Form.Item name="affirmativeActive" initialValue={isRequired}>
                <SwitchField
                  editable
                  label={'Необходимость этапа "Утверждение"'}
                  name="affirmativeActive"
                  fieldType={ModelFormFieldType.SWITCH}
                  options={yesNoOptions}
                  initialValue={state[0]?.affirmativeActive}
                />
              </Form.Item>

              <Form.Item name="tripConfirmationActive" initialValue={isRequired}>
                <SwitchField
                  editable
                  label={'Необходимость этапа "Подтверждение"'}
                  name="tripConfirmationActive"
                  fieldType={ModelFormFieldType.SWITCH}
                  options={yesNoOptions}
                  initialValue={state[0]?.tripConfirmationActive}
                />
              </Form.Item>

              <Form.Item name="tripConfirmationDocumentCheck" initialValue={isRequired}>
                <SwitchField
                  editable
                  label='Необходимость проверки документа на этапе "Подтверждение поездки"'
                  name="tripConfirmationDocumentCheck"
                  fieldType={ModelFormFieldType.SWITCH}
                  options={yesNoOptions}
                  initialValue={state[0]?.tripConfirmationDocumentCheck}
                />
              </Form.Item>
            </>
            )}

            {(selectedTransportType === TransportTypes.PERSONAL
            || selectedTransportType === TransportTypes.CARSHARING) && (
            <Form.Item name="tripApprovalActive" initialValue={isRequired}>
              <SwitchField
                editable
                label='Необходимость этапа "Утверждение"'
                name="tripApprovalActive"
                fieldType={ModelFormFieldType.SWITCH}
                options={yesNoOptions}
                initialValue={state[0]?.tripApprovalActive}
              />
            </Form.Item>
            )}

            <Form.Item name="isRequired" initialValue={isRequired}>
              <SwitchField
                editable
                label="Требуется согласование"
                name="isRequired"
                fieldType={ModelFormFieldType.SWITCH}
                onChange={onChange}
                options={yesNoOptions}
                initialValue={isRequired}
              />
            </Form.Item>
            {isRequired && (
            <>
              <button className={styles.headerAccordion} onClick={onToggleAddSettings}>
                <Icon type="settingsGear" className={styles.iconSettingsGear} />
                {t.SettingsTripRules.approvals.additionalSettingsTitle}
              </button>
              {isAddSettings && (
              <Table
                className={styles.table}
                columns={columns}
                dataSource={state}
                style={{ overflow: 'scroll' }}
                scroll={{ x: 800 }}
                footer={() => <Footer onClick={addRow} />}
                rowKey="rowId"
              />
              )}
            </>
            )}
          </div>

          {/* Эти кнопки будут переделаны, поэтому не менял на i18n */}
          <div className={styles.buttonBlock}>
            <Popconfirm
              placement="top"
              title="Отменить?"
              onConfirm={onCancel}
              okText="Да"
              cancelText="Не отменять"
              style={{ width: 300 }}
            >
              <Button
                className={styles.button}
                size="middle"
                danger
              >
                {t.global.cancel}
              </Button>
            </Popconfirm>

            <Popconfirm
              placement="top"
              title="Вернуть значения по умолчанию?"
              onConfirm={onDefault}
              okText="Да"
              cancelText="Не возвращать"
              style={{ width: 300 }}
            >
              <Button
                className={styles.button}
                size="middle"
                htmlType="submit"
              >
                {t.global.defaultValues}
              </Button>
            </Popconfirm>

            <Button
              icon={<SaveOutlined />}
              size="middle"
              type="primary"
              onClick={() => onFinish()}
            >
              {t.global.save}
            </Button>
          </div>
        </div>
      </Form>
    </div>
  );
};

export default Approvals;
