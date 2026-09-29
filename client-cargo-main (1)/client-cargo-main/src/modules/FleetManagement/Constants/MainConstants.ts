enum FleetManagementLinks {
  fleetManagement = 'fleetManagement',
  parkingsInfo = 'info',
  repair = 'repair',
  maintenance = 'my-repairs',
  tireFitting = 'tireFitting',
  evacuation = 'evacuation',
  washing = 'washing',
  accident = 'accident',
  parkingPay = 'parkingPay',
}

const FleetManagementLinksTitles = {
  [FleetManagementLinks.fleetManagement]: 'Парковки',
  [FleetManagementLinks.repair]: 'Ремонт',
  [FleetManagementLinks.maintenance]: 'Мои ремонты',
  [FleetManagementLinks.tireFitting]: 'Шиномонтаж',
  [FleetManagementLinks.evacuation]: 'Эвакуация',
  [FleetManagementLinks.washing]: 'Мойка',
  [FleetManagementLinks.accident]: 'ДТП',
  [FleetManagementLinks.parkingPay]: 'Оплатить парковку',
};

export { FleetManagementLinks, FleetManagementLinksTitles };
