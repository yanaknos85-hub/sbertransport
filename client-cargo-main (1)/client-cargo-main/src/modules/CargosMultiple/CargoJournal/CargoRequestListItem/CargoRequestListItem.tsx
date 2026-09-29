import React, { FC, useEffect, useState } from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import {
  Checkbox,
  Divider, Form, List, Space, Tooltip
} from 'antd';
import { CheckboxChangeEvent } from 'antd/es/checkbox';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import moment from 'moment';
import AddressBlockMulti from 'shared/components/Cargo/AddressBlockMulti/AddressBlockMulti';
import { EvaluationButtonsBlock } from 'shared/components/Cargo/AddressBlockMulti/EvaluationButtonsBlock';
import CargoTariffTag from 'shared/components/Cargo/CargoTariffTag/CargoTariffTag';
import CargoTypeIcon from 'shared/components/Cargo/CargoTypeIcon';
import StatusTag from 'shared/components/Cargo/StatusTag/StatusTag';
import UserBlock from 'shared/components/Cargo/UserBlock/UserBlock';
import { ListItemSubtitleMultiple } from 'shared/components/ListItemSubtitleMultiple/ListItemSubtitleMultiple';
import { useUiContext } from 'shared/components/UI';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ReactComponent as Repeat } from 'shared/images/cargo/repeat.svg';
import { RequestType } from 'shared/models/types';
import TButton from 'shared/ui/Button/Button';
import Arrow from 'ui/SideMenu/images/Arrow';
import { toRubles } from 'utils';

import { usePostEvaluation } from 'api/evaluation';
import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { CargoTypeNameEnum } from 'stores/CargoType/CargoType.interface';
import { CargosTabsFilters, Filters } from 'constants/Cargo.constants';
import {
  CargoRequestStatusesEnum,
  CargoRequestStatusesTitles,
  CargoRequestStatusesTypes
} from 'constants/CargoRequestStatuses.constants';
import { DATE_FORMAT } from 'constants/constants.app';
import * as routes from 'constants/constants.routes';
import { UUID } from 'utils/io-ts';

import { NOTIFICATION_DESCRIPTION, NOTIFICATION_MESSAGE } from '../../../Evaluation/constants';
import { Evaluation } from '../../../Evaluation/Evaluation';
import { openNotification } from '../../../Evaluation/utils';
import CargoApprovalBlock from '../../CargoApproval/CargoApprovalBlock';
import { CargoReceivedButton } from '../CargoReceivedButton/CargoReceivedButton';
// todo: Оставить только один файл стилей
import * as S from './styled';

import styles from './item.module.scss';

const convertDate = (date: number): string => {
  const momentDate = moment(date);
  return momentDate.calendar(null, {
    lastDay: momentDate.format(`Вчера [в] ${DATE_FORMAT.TIME_FULL}`),
    sameDay: momentDate.format(`Сегодня [в] ${DATE_FORMAT.TIME_FULL}`),
    nextDay: momentDate.format(`Завтра [в] ${DATE_FORMAT.TIME_FULL}`),
    sameElse: momentDate.format(`${DATE_FORMAT.BASE_REVERTED_DOTS} [в] ${DATE_FORMAT.TIME_FULL}`),
  });
};

/**
 * По полученному из запроса типу транспорта и типу тарифу выбираем отображаемый текст для элемента CargoTariffTag
 * @param  request запрос
 */
const getTariffText = (request: CargoRequestModel): string => request.transportTypeRus ? request.transportTypeRus : 'Не определен';

/**
 * Отображаем запрос для журнала в виде кликабельных карточек
 * @param request запрос (а точнее, ответ на него)
 * @param onClickHandler действие по клику на карточку
 * @param accentColor
 * @param rest
 * @param changeStatus функция изменения статуса
 * @param isApproval является ли компонент частью модуля согласований
 * @class
 */

interface Props {
  request: CargoRequestModel;
  onClickHandler: (id: string) => void;
  accentColor: string;
  isApproval: boolean;
  activeTab: string;
  isRegular?: boolean;
  tabFilterName?: Filters;
  onSuccess?: () => void;
}

export const RenderCargoRequestList: FC<Props> = observer(({
  request,
  onClickHandler,
  accentColor,
  isApproval,
  activeTab,
  isRegular,
  tabFilterName,
  onSuccess,
  ...rest
}) => {
  const {
    [StoreNames.cargosStore]: cargosStore,
    [StoreNames.cargoStore]: cargoStore,
    [StoreNames.cargoTariffStore]: cargoTariffStore,
    [StoreNames.geoStore]: geoStore,
  } = useAppStoreContext();

  const [visible, setVisible] = useState(false);
  const [requestStatus, setRequestStatus] = useState<number | undefined>();
  const [disabling, setDisabling] = useState(true);
  const [userListIsOpen, setUserListIsOpen] = useState(false);
  const { isMobile } = useUiContext();

  const [form] = Form.useForm();
  const [postEvaluation] = usePostEvaluation();

  const {
    checkedListApproval,
    setCheckedListApproval,
  } = cargosStore;

  useEffect(() => {
    setCheckedItem(!!checkedListApproval?.find(item => item.id === request.id));
  }, [checkedListApproval]);

  const handleFeedbackVisible = () => {
    setVisible(true);
  };
  const history = useHistory();

  const repeatOrder = async () => {
    await cargosStore.getMultipleRequestById(request.id as UUID);
    if (!cargosStore.cargoMultipleRequest) {
      return;
    }
    // Установка флага повторного заказа
    cargoStore.setRepeatOrder(true);

    cargoStore.fillForSteps(cargosStore.cargoMultipleRequest);
    geoStore.fillForSteps(cargosStore.cargoMultipleRequest);
    cargoTariffStore.fillForSteps(cargosStore.cargoMultipleRequest);
    history.push(routes.REPEAT_ORDER);
  };

  const handleFeedbackCancel = () => {
    setVisible(false);
    setDisabling(true);
    form.setFieldsValue({ rating: 0 });
    form.resetFields();
  };

  const rub = (value = 0) => `${toRubles(value, true).toFixed(2)} &#8381`;

  const submitFeedback = (values: any) => {
    const {
      comment, rating, reasons,
    } = values;

    postEvaluation({
      requestId: request.id,
      comment,
      rating,
      reasons,
    })
      .then(res => {
        setRequestStatus(res as number);
        setTimeout(
          () => openNotification({
            placement: 'bottomRight',
            message: NOTIFICATION_MESSAGE,
            description: NOTIFICATION_DESCRIPTION,
          }),
          1000
        );
        setTimeout(() => {
          window.location.pathname = `${routes.CARGOS}/active`;
        }, 500);
      })
      .catch(err => {
        setRequestStatus(err.response.status as number);
        return null;
      });

    form.resetFields();

    setTimeout(() => {
      setVisible(false);
    }, 2000);
  };

  const toggleUserListOpen = () => setUserListIsOpen(!userListIsOpen);
  // todo: Сделать отдельную модель для элемента журнала => => => сделать отдельный компонент для элемента журнала
  // Необходимо переделать модель и компонент для элемента журнала.
  // Элементы разных журналов на бэке это разные сущности, а на фронте один компонент отображается для разных журналов.
  // Такой костыль из-за того, что в ответе бэка в listCargo может быть либо строка, либо объект.
  // Причем если это строка, то передаётся тип CargoTypeNameEnum, а если объект, то CargoTypeModel.
  const cargoList = (request?.listCargo || [])
    .map(cargo => {
      if (!cargo.cargoType) {
        return cargo as unknown as CargoTypeNameEnum;
      } else {
        return cargo.cargoType;
      }
    })
    .filter((item, index, arr) => arr.indexOf(item) === index);

  const [checkedItem, setCheckedItem] = useState(false);

  const onChange = (e: CheckboxChangeEvent, id: string) => {
    setCheckedItem(e.target.checked);
    if (e.target.checked) {
      const routesForHandleSend = cargosStore['cargoMultipleApprovalList'].find(route => route.id === id) as CargoRequestModel;
      setCheckedListApproval([...checkedListApproval, routesForHandleSend]);
    } else {
      setCheckedListApproval([...checkedListApproval.filter(item => item.id !== id)]);
    }
  };

  return (
    <List.Item className={styles.listItem}>
      <S.StyledCard {...{ accentColor, ...rest }}>
        <S.Header>
          <div className={styles.anotherDiv}>
            <Space direction="vertical" size={8}>
              <div style={{ display: 'flex', flexDirection: isMobile ? 'column' : 'row' }}>
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                  <div className={styles.idDiv}>
                    <div>
                      Заявка
                      {' '}
                      {request?.humanReadableId}
                    </div>
                    <div className={styles.bage}>
                      {request.express ? (
                        <S.FireFilled />
                      ) : (
                        ''
                      )}
                    </div>
                  </div>
                </div>
                <div className={styles.creationTime}>
                  от
                  {' '}
                  {moment(request?.creationTime).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
                </div>
                <div className={styles.cargoTariffDiv}>
                  <CargoTariffTag express="true">
                    {getTariffText(request)}
                  </CargoTariffTag>
                  {request.requestType === RequestType.RELOCATION && (
                    <S.StyledTag>Переезд</S.StyledTag>
                  )}
                </div>
              </div>
              <div className={styles.flexRowIcons}>
                {request.requestType === RequestType.RELOCATION && (
                  <S.RelocationWrapper>
                    <span>
                      № служебной записки:
                      {' '}
                      {request.internalNote}
                    </span>
                  </S.RelocationWrapper>
                )}
                <div className={styles.cargoTypeIcons}>
                  {cargoList?.map(item => (
                    <CargoTypeIcon name={item} key={item} />
                  ))}
                </div>
              </div>
            </Space>
            <S.BlockInfoWrapper>
              <S.BlockInfo>
                <div>
                  <ListItemSubtitleMultiple request={request} isRegular={isRegular} />
                  {isApproval ? (
                    <Space />
                  ) : (
                    <Tooltip title={convertDate(request?.deliveryTimeDate)}>
                      <div className={styles.statusDiv}>
                        <StatusTag type={request?.status && CargoRequestStatusesTypes[request?.status]}>
                          {request?.status && CargoRequestStatusesTitles[request?.status]}
                        </StatusTag>
                      </div>
                    </Tooltip>
                  )}
                </div>
                {isRegular && (
                  <S.BlockInfoCost>
                    <p>Стоимость доставки</p>
                    <S.Cost dangerouslySetInnerHTML={{ __html: rub(request?.totalCost) }} />
                  </S.BlockInfoCost>
                )}
              </S.BlockInfo>
              {isApproval && activeTab === CargosTabsFilters.active && (
                <S.CheckboxStyled onClick={e => e.stopPropagation()}>
                  <Checkbox
                    checked={checkedItem}
                    onChange={(e: CheckboxChangeEvent) => onChange(e, request?.id as string)}
                  />
                </S.CheckboxStyled>
              )}
            </S.BlockInfoWrapper>
          </div>
        </S.Header>
        <Divider />
        <div className={styles.cargoAddressBlockDiv}>
          <div className={styles.cargoAddressDiv}>
            <AddressBlockMulti waypoints={request?.waypoints} />
          </div>
          <div className={styles.button}>
            {!isMobile && !isRegular && !isApproval && request.requestType !== RequestType.RELOCATION && (
              <Tooltip title="Повтор заявки">
                <S.IconButton
                  onClick={repeatOrder}
                  src={<Repeat />}
                />
              </Tooltip>
            )}
            <TButton
              $size="small"
              $isMobile={isMobile}
              style={{
                backgroundColor: '#F2F3F6',
                color: '#4D4D4D',
                borderColor: '#F2F3F6',
              }}
              onClick={(): void => onClickHandler(request.id)}
            >
              Подробнее
            </TButton>
            {request.status === CargoRequestStatusesEnum.CARGO_TRANSFER_FINISHED && cargoList.includes(CargoTypeNameEnum.DOCUMENT_CARS) && (
              <CargoReceivedButton
                request={request}
                onSuccess={onSuccess}
              />
            )}
            {((isApproval || !isRegular) && activeTab === CargosTabsFilters.active)
            || (isRegular && activeTab === CargosTabsFilters.active
            && (request.status === CargoRequestStatusesEnum.CARGO_AWAITING_APPROVAL || request.status === CargoRequestStatusesEnum.CARGO_APPROVED)) ? (
              <CargoApprovalBlock
                requestId={request.id}
                isJournal={true}
                isApproval={isApproval}
                isRegular={isRegular}
                request={request}
                isMobile={isMobile}
              />
              )
              : null}
            <EvaluationButtonsBlock
              handleFeedbackVisible={handleFeedbackVisible}
              request={request}
              isMobile={isMobile}
              isApproval={isApproval}
            />
          </div>
        </div>
        <Divider />
        {request.requestType === RequestType.RELOCATION ? (
          <div className={styles.emptyBlock} />
        ) : (
          <div className={styles.userContainer}>
            <div className={styles.userBlock}>
              {!isApproval && request.approvedBy?.length ? (
                (userListIsOpen ? request.approvedBy : [request.approvedBy[0]]).map((user, index) => (
                  <UserBlock
                    key={index}
                    author={user}
                    driverData={request.resolution}
                    status={request.status}
                    departmentName={user?.departmentName}
                    innerElement={<Space />}
                  />
                ))
              ) : (
                <UserBlock
                  author={request.author}
                  driverData={request.resolution}
                  status={request.status}
                  departmentName={request?.approvedBy?.map(user => user.departmentName)}
                  innerElement={<Space />}
                />
              )}
            </div>
            {!isApproval && (
              <div className={styles.userContainerDiv}>
                <div className={styles.userBlockDiv}>Согласующие</div>
                {request.approvedBy && request.approvedBy?.length !== 1 && (
                  <div className={styles.arrow} onClick={toggleUserListOpen}>
                    {' '}
                    <Arrow isRotate={userListIsOpen} />
                    {' '}
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </S.StyledCard>
      <Evaluation
        form={form}
        request={request}
        requestStatus={requestStatus}
        onCancel={handleFeedbackCancel}
        onSubmit={submitFeedback}
        onClickDetailedHandler={onClickHandler}
        title={`Посылка ${request?.humanReadableId} доставлена`}
        subTitle="Пожалуйста, оцените доставку"
        visible={visible}
        disabling={disabling}
        setDisabling={setDisabling}
      />
    </List.Item>
  );
});

export const CargoRequestListItem = React.memo(RenderCargoRequestList);
