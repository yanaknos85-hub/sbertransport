import React, { FC, useState } from 'react';
import { Row } from 'antd';
import { observer } from 'mobx-react';
import AddressBlockDetailedMulti from 'shared/components/Cargo/AddressBlockMulti/AddressBlockDetailedMulti';
import CargoCommentMulti from 'shared/components/Cargo/CargoCommentMulti';
import {
  CargoContainer,
  CargoFixed,
  CargoLayout,
  CargoLayoutWrapper,
  CargoMainContent,
  CargoMainInnerContent,
  CargoSideContent,
  CargoSideInnerContent
} from 'shared/components/Cargo/CargoLayout';
import CargoList from 'shared/components/Cargo/CargoList';
import CargoTotalMulitple from 'shared/components/Cargo/CargoTotalMulitple';
import { GoodsList } from 'shared/components/Cargo/GoodsList/GoodsList';
import Scheduler from 'shared/components/SchedulerMulti/Scheduler';
import { useUiContext } from 'shared/components/UI';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import TButton from 'shared/ui/Button/Button';

import { StepFinalValues } from 'stores/Cargo/Cargo.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import StepHeader from '../components/StepHeader/StepHeader';
import { STEPS } from '../constants';
import * as Styled from './CargoFinalStep.style';
import RelocationAuthor from './RelocationAuthor/RelocationAuthor';
import { SectionWrapper } from './SectionWrapper';

interface Props {
  step?: number;
  setStep: (param: number) => void;
  onSave: (total: StepFinalValues) => void;
  isRegular: boolean;
}

const CargoFinalStep: FC<Props> = observer(props => {
  const { setStep, onSave } = props;

  const {
    [StoreNames.geoStore]: geoStore,
    [StoreNames.cargoStore]: cargoStore,
  } = useAppStoreContext();

  const { isMobile } = useUiContext();

  const [schedulerError, setSchedulerError] = useState(false);

  const { waypoints } = geoStore;
  const { isRegular } = cargoStore;
  const {
    senderOrganization,
    sourceLoadersCount,
    recipientOrganization,
    destinationLoadersCount,
  } = cargoStore.stepAddressValues;

  const {
    desiredDate, sender, internalNote,
  } = cargoStore.stepAddressValuesMulti;

  const { cargoList } = cargoStore.stepCargosValues;
  const filteredCargoList = cargoList.filter(item => item.occupiedPlacesCount > 0);
  const occupiedPlacesCount = (
    filteredCargoList.reduce((placesCount, object) => placesCount + object.occupiedPlacesCount, 0)
  );

  // eslint-disable-next-line @stylistic/no-mixed-operators
  const isErrors = isRegular && !cargoStore.totalRegularCost || schedulerError;
  const buttonText = isErrors ? 'Необходимо задать период' : 'Оформить заказ';

  const { tariffCost, expected } = cargoStore.stepTariffValuesMulti;
  const handleSave = (total: StepFinalValues) => {
    onSave(total);
  };

  const stepBack = cargoStore.isRelocation ? STEPS.cargos : STEPS.tariff;

  return (
    <CargoLayoutWrapper>
      <CargoLayout>
        <CargoContainer>
          <CargoMainContent>
            <CargoMainInnerContent>
              <StepHeader
                title="Подтверждение заказа"
                desiredDate={desiredDate}
                onChange={() => setStep(stepBack)}
                isRegular={isRegular}
              />
            </CargoMainInnerContent>
            <CargoMainInnerContent>
              <Row>
                <Styled.ColAddress>
                  <AddressBlockDetailedMulti
                    waypoints={waypoints}
                    employees={cargoStore.waypointsListMulti}
                    senderOrganization={senderOrganization}
                    recipientOrganization={recipientOrganization}
                  />
                </Styled.ColAddress>
                <Styled.ColAddress>
                  <Styled.Map
                    markers={geoStore.waypoints}
                    polylines={geoStore?.calculatedRoute?.segments}
                    dragging={true}
                    zoomControl={true}
                  />
                </Styled.ColAddress>
              </Row>
            </CargoMainInnerContent>
            {cargoStore.isRelocation && (
              <SectionWrapper title="Заказчик">
                <RelocationAuthor
                  sender={sender}
                  internalNote={internalNote}
                />
              </SectionWrapper>
            )}
            <SectionWrapper title="Грузы" count={occupiedPlacesCount}>
              <div>
                <CargoList list={filteredCargoList || []} />
              </div>
            </SectionWrapper>
            {cargoStore.isRelocation && (cargoStore.packageForm.packages?.length ?? 0) > 0 && (
              <SectionWrapper title="Дополнительные услуги">
                <GoodsList />
              </SectionWrapper>
            )}
            <CargoCommentMulti />
            {isMobile && (
              <>
                {isRegular && (
                  <CargoSideInnerContent>
                    <Scheduler
                      tariffCost={tariffCost?.cost}
                      setSchedulerError={setSchedulerError}
                    />
                  </CargoSideInnerContent>
                )}
                <CargoTotalMulitple
                  cargoList={cargoList || []}
                  packageList={cargoStore.packageForm.packages}
                  expected={expected || null}
                  tariffCost={tariffCost || null}
                  sourceLoadersCount={sourceLoadersCount || 0}
                  destinationLoadersCount={destinationLoadersCount || 0}
                  allCost={cargoStore.totalRegularCost}
                  button={total => {
                    return (
                      <TButton
                        disabled={isErrors}
                        $fontWeight="normal"
                        htmlType="submit"
                        onClick={() => handleSave(total)}
                      >
                        {buttonText}
                      </TButton>
                    );
                  }}
                  isRegular={isRegular}
                />
              </>
            )}
          </CargoMainContent>
          {!isMobile && (
            <CargoSideContent>
              <CargoFixed>
                {isRegular && (
                  <CargoSideInnerContent>
                    <Scheduler
                      tariffCost={tariffCost?.cost}
                      setSchedulerError={setSchedulerError}
                    />
                  </CargoSideInnerContent>
                )}
                <CargoTotalMulitple
                  cargoList={cargoList || []}
                  packageList={cargoStore.packageForm.packages}
                  expected={expected || null}
                  tariffCost={tariffCost || null}
                  sourceLoadersCount={sourceLoadersCount || 0}
                  destinationLoadersCount={destinationLoadersCount || 0}
                  allCost={cargoStore.totalRegularCost}
                  button={total => {
                    return (
                      <TButton
                        disabled={isErrors}
                        $fontWeight="normal"
                        htmlType="submit"
                        onClick={() => handleSave(total)}
                      >
                        {buttonText}
                      </TButton>
                    );
                  }}
                  isRegular={isRegular}
                />
              </CargoFixed>
            </CargoSideContent>
          )}
        </CargoContainer>
      </CargoLayout>
    </CargoLayoutWrapper>
  );
});

export default CargoFinalStep;
