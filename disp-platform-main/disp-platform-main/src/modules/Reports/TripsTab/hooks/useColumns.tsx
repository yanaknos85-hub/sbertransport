import React, { useMemo } from 'react';
import { ColumnType } from 'antd/lib/table';
import { Link } from 'react-router-dom';
import moment from 'moment';

import { useTranslation } from 'i18n';
import { TRIP } from 'constants/routes.constants';
import { TripsReport } from 'api/trips-reports/trips-reports.types';
import { DirectionMapSortToOrder, DATE_FORMAT } from 'constants/app.constants';
import { convertSnakeToCamelCase } from 'utils/convertStringCase';
import { useTripsTabQuery } from '../context/TripsTab.queryContext';
import { Columns } from '../constants/TripsTab.constants';

const useColumns = (): ColumnType<TripsReport>[] => {
  const { field, direction } = useTripsTabQuery().query;
  const { t } = useTranslation();

  // TODO 33005, authorFullName и factCost временно скрываются, пока нет данных с бэка
  return useMemo((): ColumnType<TripsReport>[] => [
    {
      title: t.Reports.HumanReadableId,
      dataIndex: Columns.HumanReadableId,
      key: Columns.HumanReadableId,
      render: (humanReadableId, { id }) => <Link to={TRIP.replace(':id', id)}>{humanReadableId}</Link>,
      width: 156,
    },
    {
      title: t.Reports.RequestHumanReadableId,
      dataIndex: Columns.RequestHumanReadableId,
      key: Columns.RequestHumanReadableId,
      width: 200,
      sortDirections: ['ascend', 'descend'],
      sorter: true,
      sortOrder: (
        field && convertSnakeToCamelCase(field) === 'requestHumanReadableId' && direction
          ? DirectionMapSortToOrder[direction]
          : null
      ),
    },
    {
      title: t.Reports.DesiredDate,
      dataIndex: Columns.DesiredDate,
      key: Columns.DesiredDate,
      width: 219,
      sortDirections: ['ascend', 'descend'],
      sorter: true,
      sortOrder: (
        field && convertSnakeToCamelCase(field) === 'desiredDate' && direction
          ? DirectionMapSortToOrder[direction]
          : null
      ),
      render: desiredDate => desiredDate && moment(desiredDate).format(DATE_FORMAT.DATE_WITH_TIME),
    },
    {
      title: t.Reports.RouteStart,
      dataIndex: Columns.RouteStart,
      key: Columns.RouteStart,
      render: waypoints => waypoints.map((waypoint, index) => (
        <div key={index}>{waypoint}</div>
      )),
      width: 420,
    },
    {
      title: t.Reports.RouteWaypoints,
      dataIndex: Columns.RouteWaypoints,
      key: Columns.RouteWaypoints,
      render: waypoints => waypoints.map((waypoint, index) => (
        <div key={index}>{waypoint}</div>
      )),
      width: 420,
    },
    {
      title: t.Reports.RouteEnd,
      dataIndex: Columns.RouteEnd,
      key: Columns.RouteEnd,
      render: waypoints => waypoints.map((waypoint, index) => (
        <div key={index}>{waypoint}</div>
      )),
      width: 420,
    },
    {
      title: t.Reports.ExpectedTime,
      dataIndex: Columns.ExpectedTime,
      key: Columns.ExpectedTime,
      width: 167,
    },
    {
      title: t.Reports.ExpectedWaitTime,
      dataIndex: Columns.ExpectedWaitTime,
      key: Columns.ExpectedWaitTime,
      width: 172,
    },
    {
      title: t.Reports.ExpectedDistance,
      dataIndex: Columns.ExpectedDistance,
      key: Columns.ExpectedDistance,
      width: 211,
    },
    {
      title: t.Reports.MinRideDistanceCost,
      dataIndex: Columns.MinRideDistanceCost,
      key: Columns.MinRideDistanceCost,
      width: 208,
    },
    {
      title: t.Reports.WaitCostPerMin,
      dataIndex: Columns.WaitCostPerMin,
      key: Columns.WaitCostPerMin,
      width: 168,
    },
    {
      title: t.Reports.RideCostPerKm,
      dataIndex: Columns.RideCostPerKm,
      key: Columns.RideCostPerKm,
      width: 128,
    },
    {
      title: t.Reports.TotalWithoutVAT,
      dataIndex: Columns.TotalWithoutVAT,
      key: Columns.TotalWithoutVAT,
      width: 140,
    },
    {
      title: t.Reports.Status,
      dataIndex: Columns.Status,
      key: Columns.Status,
      width: 135,
    },
    {
      title: t.Reports.Comment,
      dataIndex: Columns.Comment,
      key: Columns.Comment,
      width: 400,
    },
    {
      title: t.Reports.TripType,
      dataIndex: Columns.TripType,
      key: Columns.TripType,
      width: 141,
    },
    {
      title: t.Reports.ContractNumber,
      dataIndex: Columns.ContractNumber,
      key: Columns.ContractNumber,
      width: 144,
    },
    {
      title: t.Reports.DriverFullName,
      dataIndex: Columns.DriverFullName,
      key: Columns.DriverFullName,
      width: 340,
    },
    {
      title: t.Reports.VehicleNumber,
      dataIndex: Columns.VehicleNumber,
      key: Columns.VehicleNumber,
      width: 150,
    },
    {
      title: t.Reports.PassengerFullNames,
      dataIndex: Columns.PassengerFullNames,
      key: Columns.PassengerFullNames,
      render: passengers => passengers.map((pass, index) => (
        <div key={index}>{pass}</div>
      )),
      width: 340,
    },
    // {
    // title: t.Reports.AuthorFullName,
    //  dataIndex: Columns.AuthorFullName,
    //   key: Columns.AuthorFullName,
    //  width: 340,
    // },
    {
      title: t.Reports.CreationTime,
      dataIndex: Columns.CreationTime,
      key: Columns.CreationTime,
      width: 155,
      render: creationTime => creationTime && moment(creationTime).format(DATE_FORMAT.DATE_WITH_TIME),
    },
    {
      title: t.Reports.FactDistance,
      dataIndex: Columns.FactDistance,
      key: Columns.FactDistance,
      width: 142,
      render: (distance?: number) => distance?.toFixed(2),
    },
    // {
    //  title: t.Reports.FactCost,
    //  dataIndex: Columns.FactCost,
    //   key: Columns.FactCost,
    //   width: 134,
    //  },
    {
      title: t.Reports.FactTime,
      dataIndex: Columns.FactTime,
      key: Columns.FactTime,
      width: 167,
    },
    {
      title: t.Reports.FactWaitTime,
      dataIndex: Columns.FactWaitTime,
      key: Columns.FactWaitTime,
      width: 241,
    },
  ], [direction, field, t]);
};

export default useColumns;
