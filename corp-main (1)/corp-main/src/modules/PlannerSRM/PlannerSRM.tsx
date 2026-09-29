import React from 'react';
import mfLoader from 'mf/MFLoader';

const Planner = React.lazy(() => mfLoader(import('passengers/modules/PlannerSRM')));

const PlannerSRM = () => {
  return (
    <>
      <Planner />
    </>
  );
};

export default PlannerSRM;
