import React from 'react';

import ApprovalDetailed from './CargoApproval/CargosApprovalDetailed';
import ApprovalJournal from './CargoApproval/CargosApprovalJournal';
import CargosApprovalTabs from './CargoApproval/CargosApprovalTabs';
import RequestDetailed from './CargosRequestDetailed';
import RequestJournal from './CargosRequestJournal';

const CargosApprovalJournal = () => <ApprovalJournal />;
const CargosApprovalTabsJournal = () => <CargosApprovalTabs />;
const CargosApprovalDetailed = () => <ApprovalDetailed />;
const CargosRequestJournal = () => <RequestJournal />;
const CargosRequestDetailed = () => <RequestDetailed />;

const RegularCargosApprovalJournal = () => <ApprovalJournal isRegular={true} />;
const RegularCargosApprovalDetailed = () => <ApprovalDetailed isRegular={true} />;
const RegularCargosRequestJournal = () => <RequestJournal isRegular={true} />;
const RegularCargosRequestDetailed = () => <RequestDetailed isRegular={true} />;

export {
  CargosApprovalDetailed,
  CargosApprovalJournal,
  CargosApprovalTabsJournal,
  CargosRequestDetailed,
  CargosRequestJournal,
  RegularCargosApprovalDetailed,
  RegularCargosApprovalJournal,
  RegularCargosRequestDetailed,
  RegularCargosRequestJournal
};
