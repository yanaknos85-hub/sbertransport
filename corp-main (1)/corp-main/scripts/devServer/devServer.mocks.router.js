const express = require('express');

const cargoTypeNames = require('./data/cargoTypeNames.json');
const cargoTypes = require('./data/cargoTypes.json');
const cargoType = require('./data/cargoType.json');
const orgForTree = require('./data/orgForTree.json');
const orgForTreeSecondLevel = require('./data/orgForTreeSecondLevel.json');

const repairMonitoring = require('./data/repair/repairMonitoring.json');
const repair = require('./data/repair/repair.json');
const checkWorkOrder = require('./data/repair/checkWorkOrder.json');

const router = new express.Router();

router.get('/organizations/cargo/type/types', (req, res) => {
  res.send(cargoTypeNames);
});

router.get('/organizations/cargo/type', (req, res) => {
  res.send(cargoTypes);
});

router.post('/organizations/cargo/type', (req, res) => {
  res.send(cargoType);
});

router.put('/organizations/cargo/type/:id', (req, res) => {
  res.send(cargoType);
});

router.delete('/organizations/cargo/type/:id', (req, res) => {
  res.sendStatus(200)
});

router.post('/reports/analytics/filters', (req, res) => {
  console.log(req.body.id)
  if(req.body.id == 1 || req.body.id == 2) {
      res.send(orgForTree);
  }
  res.send(orgForTreeSecondLevel)
});

router.post('/repair/monitoring', (req, res) => {
  res.send(repairMonitoring);
});

router.get('/repair/request/:repairID', (req, res) => {
  res.send(repair);
});

router.post('/repair/monitoring/:repairID/take_to_work', (req, res) => {
  res.sendStatus(200);
});

router.patch('/repair/monitoring/request/:repairID', (req, res) => {
  res.sendStatus(200);
});

router.delete('/repair/monitoring/:orderId/work_order', (req, res) => {
  // deleteWorkOrder
  res.sendStatus(200);
});

router.patch('/repair/monitoring/request/:orderId', (req, res) => {
  // saveChangeOrder
  res.sendStatus(200);
});

router.post('/repair/monitoring/:orderId/work_order_check', (req, res) => {
  // checkWorkOrder
  res.send(checkWorkOrder);
});

router.get('/repair/monitoring/:orderId/work_order', (req, res) => {
  // getWorkOrder
  res.send(['file_work_order']);
});

module.exports = router;
