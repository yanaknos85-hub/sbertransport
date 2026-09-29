export enum VisibleFields {
  humanReadableId = 'routelistIdVisible',
  status = 'statusVisible',
  author = 'authorVisible',

  timeZone = 'timeZoneVisible',

  creationTime = 'creationTimeVisible',
  desiredDate = 'desiredDateVisible',
  shipmentTime = 'shipmentTimeVisible',
  distance = 'distanceVisible',

  weight = 'weightVisible',
  volume = 'volumeVisible',

  cost = 'costVisible',
  actualCost = 'actualCostVisible',
  planedRangeVisible = 'planedRangeVisible',
  waypointCountVisible = 'waypointCountVisible',

  organizations = 'organizationsVisible',
  contractor = 'contractorNameVisible',
  capacity = 'capacityVisible',
  loaders = 'loadersVisible',

  driver = 'driverVisible',
  driverPhone = 'driverPhoneVisible',
  carInfo = 'carInfoVisible',
  active = 'activeVisible',
}

/**
 * Fields which cam be sorted in register table.
 * Values are backend sort parameters.
 */
export const sortFields: { [key in VisibleFields]?: string } = {
  [VisibleFields.humanReadableId]: 'REQUEST_HUMAN_ID',
  [VisibleFields.shipmentTime]: 'DESIRED_DATE',
};
