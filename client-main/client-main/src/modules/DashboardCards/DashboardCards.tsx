import { observer } from 'mobx-react';
import * as React from 'react';
import Services from './components/services/Services';
import UpcomingApplications from './components/upcomingApplications/UpcomingApplications';
// import PhoneConfirmationAlert from './components/PhoneConfirmationAlert/PhoneConfirmationAlert';
import Widgets from './components/widgets/Widgets';
import UsefulMaterials from './components/usefulMaterials/UsefulMaterials';
import FAQ from './components/faq/FAQ';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { StoreNames } from 'stores';
import AuthBanner from './components/AuthBanner/AuthBanner';
import PhoneConfirmationAlert from './components/PhoneConfirmationAlert/PhoneConfirmationAlert';
import AICard from './components/AICard/AICard';

const DashboardCards: React.FC = observer(() => {
  const { [StoreNames.configStore]: configStore } = useAppStoreContext();

  const IS_PERSONAL_DEVICE = configStore.env.IS_PERSONAL_DEVICE;

  return (
    <>
      <AICard />
      <ErrorBoundary>
        <PhoneConfirmationAlert />
      </ErrorBoundary>
      <ErrorBoundary>
        <Services />
      </ErrorBoundary>
      <ErrorBoundary>
        <UpcomingApplications />
      </ErrorBoundary>
      <ErrorBoundary>
        <Widgets />
      </ErrorBoundary>
      {!IS_PERSONAL_DEVICE && (
        <ErrorBoundary>
          <UsefulMaterials />
        </ErrorBoundary>
      )}
      <ErrorBoundary>
        <FAQ />
      </ErrorBoundary>
      <AuthBanner />
    </>
  );
});

export default DashboardCards;
