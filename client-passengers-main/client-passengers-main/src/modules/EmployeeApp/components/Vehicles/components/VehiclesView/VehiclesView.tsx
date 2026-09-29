/* eslint-disable @typescript-eslint/no-explicit-any */
import { Form, Popconfirm } from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useCallback } from 'react';

import { VEHICLES_PDN } from 'constants/constants.env';
import * as routes from 'constants/constants.routes';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { Label } from 'shared/form/Field/Field.style';
import Button from 'shared/form/Button/Button';
import FormField from 'shared/form/FormField/FormField';
import { useDownloadUrl } from 'shared/hooks/useDownloadUrl';

import { Ownership } from '../../constants/vehicles.constants';
import Col, { Size } from './components/Grid/Col/Col';
import Fields from './components/Grid/Fields/Fields';
import Row from './components/Grid/Row/Row';
import UploadedFile from './components/UploadedFile/UploadedFile';

import { FieldsEnum, fields } from './constants/vehiclesForm.fields';

import useVehiclesForm from './hooks/useVehiclesForm';
import Alert from 'modules/EmployeeApp/components/Alert/Alert';
import { ReactComponent as IconBack } from './images/back.svg';

import styles from './vehiclesView.module.scss';
import ArrowRight from 'shared/components/Images/arrowRight.svg';

const VehiclesView: FC = observer(() => {
  const { configStore } = useAppStoreContext();

  const { history } = configStore;

  const {
    name,
    isEdit,
    form,
    files,
    handleUpload,
    values,
    error,
    initialValues,
    handleChange,
    handleSave,
    handleDelete,
    setFiles,
  } = useVehiclesForm();

  const { download: downloadTemplate } = useDownloadUrl(VEHICLES_PDN);

  const goBack = useCallback(() => history.push(routes.PROFILE), [history]);

  return (
    <>
      <div className={styles.headerTripRequest}>
        <span onClick={() => history.push(`${routes.EXTERNAL}/create`)}>Главная</span>
        <img src={ArrowRight} alt="Arrow" />
        <span onClick={() => history.push(`${routes.EXTERNAL}/profile`)}>Личный кабинет</span>
        <img src={ArrowRight} alt="Arrow" />
        <span>{isEdit ? 'Редактирование транспорта' : 'Добавление транспорта'}</span>
      </div>
      <div className={styles.container}>
        <div className={styles.viewport}>
          <div className={styles.content}>
            <div className={styles.title}>
              <IconBack onClick={goBack} />
              {isEdit ? 'Редактирование' : 'Добавление'}
              {' '}
              транспорта
            </div>
            <Form
              name={name}
              form={form}
              initialValues={initialValues}
              onValuesChange={handleChange}
            >
              <Fields title="Собственность">
                <FormField {...fields[FieldsEnum.ownership]} />
              </Fields>

              {/* <Fields>
                <FormField {...fields[FieldsEnum.asMain]} />
              </Fields> */}

              {(values[FieldsEnum.ownership] === Ownership.SPOUSE
              || values[FieldsEnum.ownership] === Ownership.THIRD_PARTY) && (
              <>
                {values[FieldsEnum.ownership] === Ownership.SPOUSE && (
                <Fields title="Свидетельство о браке">
                  <Row>
                    <Col>
                      <FormField
                        placeholderText="I-ПК"
                        upperCase={true}
                        withoutSpaces={true}
                        {...fields[FieldsEnum.docs_mc_series]}
                      />
                    </Col>
                    <Col>
                      <FormField
                        placeholderText="777888"
                        withoutSpaces={true}
                        {...fields[FieldsEnum.docs_mc_number]}
                      />
                    </Col>
                    <Col>
                      <FormField {...fields[FieldsEnum.docs_mc_dateOfIssue]} />
                    </Col>
                  </Row>
                </Fields>
                )}

                <Fields
                  title="Согласие на передачу персональных данных"
                  caption={(
                    <>
                      Необходимо
                      {' '}
                      <a href="#pdn" onClick={downloadTemplate}>
                        скачать шаблон ПДн
                      </a>
                      , заполнить его и прикрепить скан-копию (на основании ФЗ №152 от 27.07.2006)
                    </>
                      )}
                >
                  <Row>
                    <Col>
                      <FormField
                        {...fields[FieldsEnum.docs_pdn_file]}
                        label={
                              (values[FieldsEnum.docs_pdn_file] || []).length
                                ? null
                                : fields[FieldsEnum.docs_pdn_file].label
                            }
                        params={{
                          ...fields[FieldsEnum.docs_pdn_file].params,
                          customRequest: (props: any) => handleUpload({ ...props, field: FieldsEnum.docs_pdn_file }),
                          itemRender: (a: any, file: any, c: any, actions: any) => files[FieldsEnum.docs_pdn_file]?.fileName && (
                          <UploadedFile
                            setFiles={() => setFiles({ ...files, [FieldsEnum.docs_pdn_file]: null })}
                            file={files[FieldsEnum.docs_pdn_file]}
                            actions={actions}
                          />
                          ),
                        }}
                      />
                    </Col>
                  </Row>
                </Fields>
              </>
              )}

              <Fields title="Автомобиль">
                <Row>
                  <Col>
                    <FormField
                      placeholderText="С111ТН077"
                      upperCase={true}
                      withoutSpaces={true}
                      {...fields[FieldsEnum.vcRegistrationNumber]}
                    />
                  </Col>
                  <Col>
                    <FormField
                      placeholderText="Цвет ТС указывать, как в ПТС, например Серебряный"
                      {...fields[FieldsEnum.vcColor]}
                    />
                  </Col>
                  <Col>
                    <FormField
                      placeholderText="4 места"
                      {...fields[FieldsEnum.vcPassengerSeatsCount]}
                    />
                  </Col>
                </Row>
              </Fields>

              <Fields title="Водительское удостоверение">
                <Row>
                  <Col>
                    <FormField {...fields[FieldsEnum.docs_dl_fio]} />
                  </Col>
                  <Col size={Size.small}>
                    <FormField {...fields[FieldsEnum.docs_dl_dateOfIssue]} />
                  </Col>
                  <Col size={Size.small}>
                    <FormField {...fields[FieldsEnum.docs_dl_validUntil]} />
                  </Col>
                </Row>
                <Row>
                  <Col size={Size.small}>
                    <FormField
                      placeholderText="77АО"
                      upperCase={true}
                      withoutSpaces={true}
                      {...fields[FieldsEnum.docs_dl_series]}
                    />
                  </Col>
                  <Col size={Size.small}>
                    <FormField
                      placeholderText="100009"
                      withoutSpaces={true}
                      {...fields[FieldsEnum.docs_dl_number]}
                    />
                  </Col>
                  <Col>
                    <FormField
                      placeholderText="ГИБДД 7789"
                      upperCase={true}
                      {...fields[FieldsEnum.docs_dl_issuedBy]}
                    />
                  </Col>
                  <Col>
                    <FormField
                      placeholderText="Ростовская область"
                      upperCase={true}
                      {...fields[FieldsEnum.docs_dl_whereIssued]}
                    />
                  </Col>
                  <Col size={Size.small}>
                    <FormField
                      placeholderText="В1, С, С1"
                      upperCase={true}
                      {...fields[FieldsEnum.docs_dl_category]}
                    />
                  </Col>
                </Row>
                <Row>
                  <Col>
                    <Label>Прикрепите фотографию вашего водительского удостоверения</Label>
                    <FormField
                      {...fields[FieldsEnum.docs_dl_file]}
                      label={
                        (values[FieldsEnum.docs_dl_file] || []).length ? null : fields[FieldsEnum.docs_dl_file].label
                      }
                      params={{
                        ...fields[FieldsEnum.docs_dl_file].params,
                        customRequest: (props: any) => handleUpload({ ...props, field: FieldsEnum.docs_dl_file }),
                        itemRender: (a: any, file: any, c: any, actions: any) => files[FieldsEnum.docs_dl_file]?.fileName && (
                          <UploadedFile
                            setFiles={() => setFiles({ ...files, [FieldsEnum.docs_dl_file]: null })}
                            file={files[FieldsEnum.docs_dl_file]}
                            actions={actions}
                          />
                        ),
                      }}
                    />
                  </Col>
                </Row>
              </Fields>

              <Fields title="ПТС">
                <Row>
                  <Col>
                    <FormField
                      placeholderText="WAUZZZ44ZEN096063"
                      withoutSpaces={true}
                      {...fields[FieldsEnum.docs_pts_vin]}
                      params={{
                        ...fields[FieldsEnum.docs_pts_vin].params,
                        disabled: isEdit,
                      }}
                    />
                  </Col>
                  <Col>
                    <FormField
                      placeholderText="Volkswagen"
                      upperCase={true}
                      {...fields[FieldsEnum.docs_pts_brandName]}
                      params={{
                        ...fields[FieldsEnum.docs_pts_brandName].params,
                        disabled: isEdit,
                      }}
                    />
                  </Col>
                  <Col>
                    <FormField
                      placeholderText="Golf Sportsvan"
                      upperCase={true}
                      {...fields[FieldsEnum.docs_pts_model]}
                      params={{
                        ...fields[FieldsEnum.docs_pts_model].params,
                        disabled: isEdit,
                      }}
                    />
                  </Col>
                  <Col size={Size.small}>
                    <FormField
                      placeholderText="2100"
                      withoutSpaces={true}
                      {...fields[FieldsEnum.docs_pts_engineVolume]}
                      params={{
                        ...fields[FieldsEnum.docs_pts_engineVolume].params,
                        disabled: isEdit,
                      }}
                    />
                  </Col>
                  <Col size={Size.small}>
                    <FormField
                      placeholderText="104,7"
                      withoutSpaces={true}
                      {...fields[FieldsEnum.docs_pts_enginePower]}
                      params={{
                        ...fields[FieldsEnum.docs_pts_enginePower].params,
                        disabled: isEdit,
                      }}
                    />
                  </Col>
                </Row>
                <Row>
                  <Col>
                    <Label>
                      Прикрепите фотографию части паспорта транспортного средства на которой указаны только сведения о
                      транспортном средстве, исключая в фотографии сведения о собственниках.
                    </Label>
                    <FormField
                      {...fields[FieldsEnum.docs_pts_file]}
                      label={
                        (values[FieldsEnum.docs_pts_file] || []).length ? null : fields[FieldsEnum.docs_pts_file].label
                      }
                      params={{
                        ...fields[FieldsEnum.docs_pts_file].params,
                        disabled: isEdit,
                        customRequest: (props: any) => handleUpload({ ...props, field: FieldsEnum.docs_pts_file }),
                        itemRender: (a: any, file: any, c: any, actions: any) => files[FieldsEnum.docs_pts_file]?.fileName && (
                          <UploadedFile
                            setFiles={() => setFiles({ ...files, [FieldsEnum.docs_pts_file]: null })}
                            file={files[FieldsEnum.docs_pts_file]}
                            actions={actions}
                          />
                        ),
                      }}
                    />
                  </Col>
                </Row>
              </Fields>

              <Fields title="Полис ОСАГО">
                <Row>
                  <Col>
                    <FormField
                      placeholderText="ААА"
                      upperCase={true}
                      withoutSpaces={true}
                      {...fields[FieldsEnum.docs_osago_series]}
                    />
                  </Col>
                  <Col>
                    <FormField
                      placeholderText="1234567890"
                      withoutSpaces={true}
                      {...fields[FieldsEnum.docs_osago_number]}
                    />
                  </Col>
                  <Col size={Size.small}>
                    <FormField {...fields[FieldsEnum.docs_osago_startTime]} />
                  </Col>
                  <Col size={Size.small}>
                    <FormField {...fields[FieldsEnum.docs_osago_endTime]} />
                  </Col>
                </Row>
                <Row>
                  <Col>
                    <Label>Прикрепите фотографию или электронную версию полиса ОСАГО</Label>

                    <Alert
                      type="warning"
                      showIcon
                      className={styles.alert}
                      description="После окончания срока действия полиса не забудьте обновить данные по полису"
                    />

                    <FormField
                      {...fields[FieldsEnum.docs_osago_file]}
                      label={
                        (values[FieldsEnum.docs_osago_file] || []).length
                          ? null
                          : fields[FieldsEnum.docs_osago_file].label
                      }
                      params={{
                        ...fields[FieldsEnum.docs_osago_file].params,
                        customRequest: (props: any) => handleUpload({ ...props, field: FieldsEnum.docs_osago_file }),
                        itemRender: (a: any, file: any, c: any, actions: any) => files[FieldsEnum.docs_osago_file]?.fileName && (
                          <UploadedFile
                            setFiles={() => setFiles({ ...files, [FieldsEnum.docs_osago_file]: null })}
                            file={files[FieldsEnum.docs_osago_file]}
                            actions={actions}
                          />
                        ),
                      }}
                    />
                  </Col>
                </Row>
              </Fields>

              <Fields className={styles.agreements}>
                <FormField {...fields[FieldsEnum.confirmaDataAccuracy]} />
                <FormField {...fields[FieldsEnum.agreementPersonalData]} />
              </Fields>
            </Form>
          </div>

          <div className={styles.error}>{error}</div>

          <div className={styles.buttons}>
            <div>
              {isEdit && (
                <Popconfirm
                  className={styles.btnDanger}
                  placement="left"
                  title="Вы уверены?"
                  onConfirm={() => handleDelete().then(goBack)}
                  okText="Да"
                  cancelText="Нет"
                >
                  Удалить транспортное средство
                </Popconfirm>
              )}
            </div>
            <div>
              <Button onClick={goBack} color="transparent">
                Отмена
              </Button>
              <Button disabled={!!error} onClick={handleSave}>
                Сохранить
              </Button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
});

export default function VehiclesViewWithBoundary() {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<SpinWrapped />}>
        <VehiclesView />
      </React.Suspense>
    </ErrorBoundary>
  );
}
