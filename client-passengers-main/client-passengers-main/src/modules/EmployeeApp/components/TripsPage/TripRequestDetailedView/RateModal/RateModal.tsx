import './overwrite.scss';

import Icon, {
  SmileOutlined
} from '@ant-design/icons';
import {
  Button, Checkbox, Form, Input, Modal, Rate
} from 'antd';
import React, {
  Dispatch, SetStateAction, useEffect, useState
} from 'react';
import Close from 'shared/components/Images/Close.svg';
import { TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { ReactComponent as Clearly } from 'shared/components/Images/iconRate/clearly.svg';
import { ReactComponent as Comfortable } from 'shared/components/Images/iconRate/comfortable.svg';
import { ReactComponent as NotConvenient } from 'shared/components/Images/iconRate/notConvenient.svg';
import { ReactComponent as Quickly } from 'shared/components/Images/iconRate/quickly.svg';
import { ReactComponent as Slow } from 'shared/components/Images/iconRate/slow.svg';
import { ReactComponent as Unclear } from 'shared/components/Images/iconRate/unclear.svg';
import { ReactComponent as AllDocuments } from 'shared/components/Images/iconRate/allDocuments.svg';
import { ReactComponent as FaultyCar } from 'shared/components/Images/iconRate/faultyCar.svg';
import { ReactComponent as NoNecessaryDocuments } from 'shared/components/Images/iconRate/noNecessaryDocuments.svg';
import { ReactComponent as Dirty } from 'shared/components/Images/iconRate/dirty.svg';
import { ReactComponent as ServiceableCar } from 'shared/components/Images/iconRate/serviceableCar.svg';
import { ReactComponent as ConvenientSupport } from 'shared/components/Images/iconRate/convenientSupport.svg';
import { ReactComponent as CleanCar } from 'shared/components/Images/iconRate/cleanCar.svg';
import { ReactComponent as PoorSupport } from 'shared/components/Images/iconRate/poorSupport.svg';
import { ReactComponent as GoodMood } from 'shared/components/Images/iconRate/goodMood.svg';
import { ReactComponent as Rude } from 'shared/components/Images/iconRate/rude.svg';
import { ReactComponent as Late } from 'shared/components/Images/iconRate/late.svg';
import { ReactComponent as Aggressive } from 'shared/components/Images/iconRate/aggressive.svg';
import Success from 'shared/components/Images/success.svg';

import {
  NegativeEmotionsTitles,
  NegativeEmotionsTitlesCarsharing,
  NegativeEmotionsTitlesPersonal,
  NegativeEmotionsTitlesPublic,
  PositiveEmotionsTitles,
  PositiveEmotionsTitlesCarsharing,
  PositiveEmotionsTitlesPersonal,
  PositiveEmotionsTitlesPublic,
  RateFeedBackTitles,
  TNegativeEmotionsKeys,
  TPositiveEmotionsKeys
} from './RateModal.constants';
import { useRate } from './useRate';

import styles from './rate.module.scss';
import { TripRequestModel } from 'stores/Trip/models';
import { zoneTime } from 'utils/trips/times';
import { DATE_FORMAT } from 'constants/constants.app';
import RouteMap from 'utils/RouteMap/RouteMap';
import { useCurrentTripRequest } from 'shared/hooks/trip';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

type TRateValues = 0 | 1 | 2 | 3 | 4 | 5;

const { TextArea } = Input;

interface Props {
  show?: boolean;
  setShowModal: Dispatch<SetStateAction<boolean>>;
  isNotDetailedRequest?: boolean;
  request: TripRequestModel;
}

export const RateModal = ({
  show, setShowModal, isNotDetailedRequest = false, request,
}: Props): JSX.Element => {
  const [rateValue, setRateValue] = useState<TRateValues>(0);
  const { refetchRequestTrip } = useCurrentTripRequest();
  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();
  const { updateRateTripRequest } = tripStore;

  const {
    isFinishRate,
    hideModal,
    showModal,
    emotions,
    onFinish,
    isPositiveRate,
    isRated,
    isModalVisible,
    changeShowDetailed,
    isNotDetailedVisible,
    changeHideDetailed,
  } = useRate(rateValue);

  useEffect(() => {
    isNotDetailedRequest && changeShowDetailed();
  }, []);

  const radioClass = `card card__label no-input-checkbox ${rateValue < 4 ? 'negativeRate' : 'positiveRate'}`;

  const EmotionToIcon = {
    [PositiveEmotionsTitles.clean]: <CleanCar />,
    [PositiveEmotionsTitles.polite]: <Comfortable />,
    [PositiveEmotionsTitles.goodMood]: <GoodMood />,
    [PositiveEmotionsTitles.careful]: <ServiceableCar />,
    [NegativeEmotionsTitles.rude]: <Rude />,
    [NegativeEmotionsTitles.dirty]: <Dirty />,
    [NegativeEmotionsTitles.late]: <Late />,
    [NegativeEmotionsTitles.aggressive]: <Aggressive />,

    [PositiveEmotionsTitlesPersonal.clearly]: <Clearly />,
    [PositiveEmotionsTitlesPersonal.comfortable]: <Comfortable />,
    [PositiveEmotionsTitlesPersonal.quickly]: <Quickly />,
    [NegativeEmotionsTitlesPersonal.unclear]: <Unclear />,
    [NegativeEmotionsTitlesPersonal.notConvenient]: <NotConvenient />,
    [NegativeEmotionsTitlesPersonal.slow]: <Slow />,

    [PositiveEmotionsTitlesCarsharing.serviceableCar]: <ServiceableCar />,
    [PositiveEmotionsTitlesCarsharing.convenientSupport]: <ConvenientSupport />,
    [PositiveEmotionsTitlesCarsharing.cleanCar]: <CleanCar />,
    [PositiveEmotionsTitlesCarsharing.allDocuments]: <AllDocuments />,
    [NegativeEmotionsTitlesCarsharing.faultyCar]: <FaultyCar />,
    [NegativeEmotionsTitlesCarsharing.poorSupport]: <PoorSupport />,
    [NegativeEmotionsTitlesCarsharing.dirtyCar]: <Dirty />,
    [NegativeEmotionsTitlesCarsharing.noNecessaryDocuments]: <NoNecessaryDocuments />,

    [PositiveEmotionsTitlesPublic.clearly]: <Clearly />,
    [PositiveEmotionsTitlesPublic.comfortable]: <Comfortable />,
    [PositiveEmotionsTitlesPublic.quickly]: <Quickly />,
    [NegativeEmotionsTitlesPublic.unclear]: <Unclear />,
    [NegativeEmotionsTitlesPublic.notConvenient]: <NotConvenient />,
    [NegativeEmotionsTitlesPublic.slow]: <Slow />,
  };

  const emotionIcon = (emotion: TNegativeEmotionsKeys | TPositiveEmotionsKeys): JSX.Element => (
    <Icon component={(): JSX.Element => EmotionToIcon[emotion] ?? <SmileOutlined />} className={styles.emotion__icon} />
  );

  useEffect(() => {
    show && showModal();
  }, [show]);

  const rateTitle = rateValue === 5 ? 'Отлично' : rateValue === 4 ? 'Хорошо' : rateValue === 3 ? 'Удовлетворительно' : (rateValue === 2 || rateValue === 1) ? 'Плохо' : 'Ваша оценка';

  const refatchTrip = () => {
    if (isFinishRate && !isNotDetailedVisible) {
      refetchRequestTrip();
      changeHideDetailed();
    } else if (isFinishRate && isNotDetailedVisible) {
      updateRateTripRequest();
    }
  };

  const onCancelModalRate = () => {
    hideModal();
    setShowModal(false);
    refatchTrip();
  };

  useEffect(() => {
    if (isFinishRate && isModalVisible) {
      setTimeout(refatchTrip, 3000);
    }
  }, [isFinishRate]);

  return (
    <div>
      <Modal
        visible={isModalVisible}
        onCancel={onCancelModalRate}
        okText="Готово"
        width={332}
        cancelButtonProps={{ style: { display: 'none' } }}
        okButtonProps={{ style: { display: 'none' } }}
        centered
        className="modalRate"
        footer={null}
      >
        { !isFinishRate
          ? (
            <Form className="no-input-form" onFinish={onFinish}>
              <div className="cardWrapper">
                <div className="header">
                  <div className="numberApplication">
                    Поездка завершена
                  </div>
                  <div className="cancel" onClick={() => { hideModal(), setShowModal(false); }}>
                    <img alt="Close" src={Close} />
                  </div>
                </div>
                <span className="titleRate">Пожалуйста, оцените сервис использования услуги</span>
                <div className="routePoints">
                  <div className="routePointsHeader">
                    <div className="routePointsTypeTransport">
                      <div className="TransportPictureWrapper">
                        <div className={request.transportType} />
                      </div>
                      <span>{request.transportType && TransportTypeTitlesEnum[request.transportType]}</span>
                    </div>
                    <div className="routePointsDate">
                      <span className="routePointsDateWrapper">
                        <span className="routePointsDateTitle">Поездка </span>
                        {zoneTime(request?.desiredDate, DATE_FORMAT.DATE_WITH_TIME_DOTS, request?.timeZone)}
                      </span>
                    </div>
                  </div>
                  <div className="rout">
                    <RouteMap addresses={request.expected.waypoints} />
                  </div>
                </div>
                <div className="rate">
                  <span className="rateTitle">{rateTitle}</span>
                  <Form.Item name="rating">
                    <Rate
                      className={rateValue < 4 ? 'rateNegativeValue' : 'ratePositiveValue'}
                      value={rateValue}
                      onChange={(val): void => setRateValue(val as TRateValues)}
                    />
                  </Form.Item>
                  {isRated && isPositiveRate && <span>{RateFeedBackTitles.positiveFeedBack}</span>}
                  {isRated && !isPositiveRate && <span>{RateFeedBackTitles.negativeFeedBack}</span>}
                  {isRated && (
                  <>
                    <div className="card__wrapper">
                      <Form.Item name="emotions">
                        <Checkbox.Group className="no-input-checkbox-group with-hor-scroll-bar">
                          {emotions && Object.values(emotions).map((emotion, key) => (
                          // eslint-disable-next-line react/no-array-index-key
                            <Checkbox
                              className={radioClass}
                              value={emotion}
                              key={key}
                            >
                              {/* FIXME react/no-array-index-key */}
                              <div className="emotion">
                                {emotionIcon(emotion as TNegativeEmotionsKeys | TPositiveEmotionsKeys)}
                                <span className="emotion__text">{emotion}</span>
                              </div>
                            </Checkbox>
                          ))}
                        </Checkbox.Group>
                      </Form.Item>
                    </div>
                    <div className="additionalWrapper">
                      <span>Комментарий</span>
                      <Form.Item name="additional">
                        <TextArea
                          maxLength={255}
                          showCount
                          rows={4}
                          placeholder={RateFeedBackTitles.comment}
                          style={{ height: 120, resize: 'none' }}
                        />
                      </Form.Item>
                    </div>
                  </>
                  )}
                </div>
                <div className="rateButton">
                  <Button
                    type="primary"
                    htmlType="submit"
                    size="middle"
                    block
                    style={{ marginBottom: 20 }}
                    disabled={!isRated}
                  >
                    Оценить
                  </Button>
                </div>
              </div>
            </Form>
          )
          : (
            <div className="successWrapper">
              <img alt="Success" src={Success} />
              <span className="successTitle">Спасибо за Ваш отзыв!</span>
              <span className="successText">Оценка успешно отправлена</span>
            </div>
          )}
      </Modal>
    </div>
  );
};
