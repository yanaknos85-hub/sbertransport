import React, { FC, useState, useEffect } from 'react';
import moment from 'moment';

import Panel from 'components/Panel/Panel';
import { DATE_FORMAT } from 'constants/app.constants';
import { Shift, ShiftsFilters, TMassCreateFirstTitleItem } from 'api/shifts/shifts.types';
import { useShifts } from 'api/shifts/shifts.api';
import { useQuery } from 'hooks/useQuery';
import { StepsEnum } from './steps/steps.enum';
import Shifts from './steps/Shifts';
import Sign from './steps/Sign';

const getTomorrowDate = () => moment().add(1, 'day').format('YYYY-MM-DD');

const defaultFilters: ShiftsFilters = {
  startDate: getTomorrowDate(),
};

const MAX_SELECTION_LIMIT = 100;

const WaybillMassCreate: FC = () => {
  const [currentStep, setCurrentStep] = useState<StepsEnum>(StepsEnum.ShiftsStep);
  const [selectedShifts, setSelectedShifts] = useState<Shift[]>([]);
  const [firstTitleResult, setFirstTitleResult] = useState<TMassCreateFirstTitleItem[] | undefined>(undefined);
  const { query, setQuery } = useQuery<ShiftsFilters>(defaultFilters, {
    noPagination: true,
  });

  const formattedStartDate = query.startDate
    ? moment(query.startDate).utc().format(`${DATE_FORMAT.ISO}[Z]`)
    : moment(defaultFilters.startDate).utc().format(`${DATE_FORMAT.ISO}[Z]`);

  const { data: shiftsData, isLoading } = useShifts(
    { startDate: formattedStartDate },
    { suspense: false }
  );

  useEffect(() => {
    if (shiftsData) {
      setSelectedShifts(shiftsData.slice(0, MAX_SELECTION_LIMIT));
    }
  }, [shiftsData]);

  return (
    <Panel cardPadding>
      {currentStep === StepsEnum.ShiftsStep && (
        <Shifts
          selectedShifts={selectedShifts}
          setSelectedShifts={setSelectedShifts}
          shiftsData={shiftsData}
          isLoading={isLoading}
          query={query}
          setQuery={setQuery}
          firstTitleResult={firstTitleResult}
          setFirstTitleResult={setFirstTitleResult}
          setCurrentStep={setCurrentStep}
          maxSelectionLimit={MAX_SELECTION_LIMIT}
        />
      )}
      {currentStep === StepsEnum.SignStep && (
        <Sign
          setCurrentStep={setCurrentStep}
          firstTitleResult={firstTitleResult}
          selectedShifts={selectedShifts}
        />
      )}
    </Panel>
  );
};

export default WaybillMassCreate;
