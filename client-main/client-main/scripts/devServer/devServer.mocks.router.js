const express = require('express');

const login = require('./data/login.json')
const getSelfEmployee = require('./data/getSelfEmployee.json')
const getTransportTypes = require('./data/getTransportTypes.json')
const getTransportOrg = require('./data/getTransportOrg.json')
const getApprovementList = require('./data/getApprovementList.json')
const getLimitsSettings = require('./data/getLimitsSettings.json')
const getLimitsByDepartment = require('./data/getLimitsByDepartment.json')
const getLimitSharing = require('./data/getLimitSharing.json')
const getEmployeeLimit = require('./data/getEmployeeLimit.json')
const getOrganizations = require('./data/getOrganizations.json')
const getEmployeesOrganizations = require('./data/getEmployeesOrganizations.json')
const getEmployeesOrganizationsSearch = require('./data/getEmployeesOrganizationsSearch.json')
const getDepartments = require('./data/getDepartments.json')
const cargoMass = require('./data/cargoMass.json')
const calcRoute = require('./data/calcRoute.json')
const tariffsCalculateAll = require('./data/tariffsCalculateAll.json')
const tariffsCalculateCourier = require('./data/tariffsCalculateCourier.json')
const tariffsCalculateDedicate = require('./data/tariffsCalculateDedicate.json')
const tariffsCalculateInterregional = require('./data/tariffsCalculateInterregional.json')
const getAddressList = require('./data/getAddressList.json')
const getAddressFavorite = require('./data/getAddressFavorite.json')
const getCargoTypes = require('./data/getCargoTypes.json')
const personalCarOsago = require('./data/personalCarOsago.json')
const getCargoRequest = require('./data/getCargoRequest.json')
const getCargoHistory = require('./data/getCargoHistory.json')
const nonTerminal = require('./data/nonTerminal.json')
const terminal = require('./data/terminal.json')
const nonTerminalRegular = require('./data/nonTerminalRegular.json')
const terminalRegular = require('./data/terminalRegular.json')
const getRegularCargoRequest = require('./data/getRegularCargoRequest.json')
const postEvaluation = require('./data/postEvaluation.json')
const getEvaluation = require('./data/getEvaluation.json')

const router = new express.Router();

router.post('/auth/login', (req, res) => {
  res.json(login);
});

router.get('/organizations/self', (req, res) => {
  res.json(getSelfEmployee);
});

router.get('/organizations', (req, res) => {
  res.json(getOrganizations);
});

router.get('/organizations/:depId/employees', (req, res) => {
  res.json(getEmployeesOrganizations);
});

router.get('/organizations/:depId/employeessearch', (req, res) => {
  res.json(getEmployeesOrganizationsSearch);
});

router.get('/organizations/transport-types', (req, res) => {
  res.json(getTransportTypes);
});

router.get('/organizations/transportorg/org/:id', (req, res) => {
  res.json(getTransportOrg);
});

router.get('organizations/self/addresses/frequently', (req, res) => {
  res.json(getAddressFrequently);
});

router.get('organizations/self/addresses/favorite', (req, res) => {
  res.json(getAddressFavorite);
});

router.get('/departments', (req, res) => {
  res.json(getDepartments);
});

router.get('/approvals/', (req, res) => {
  res.json(getApprovementList);
});

router.get('/limits/limitsettings', (req, res) => {
  res.json(getLimitsSettings);
});

router.get('/limits/emplimits/getByEmployeeAndYear/:id/year/:year', (req, res) => {
  res.json(getEmployeeLimit);
});

router.get('/limits/deplimits/getByDepartment/:id', (req, res) => {
  res.json(getLimitsByDepartment);
});

router.get('/limits/limitsharing/getByLimit/full/:id', (req, res) => {
  res.json(getLimitSharing);
});

let rid = 1;

router.get('/requests/import/:id', (req, res) => {
  let resBody = {
    ...cargoMass,
  }

  if (rid >= 2) {
    resBody = {
      ...resBody,
      status: 'SUCCESS',
      // status: 'ERROR',
    }
    rid = 1;

  } else {
    resBody = {
      ...resBody,
      status: 'IN_PROGRESS',
      data: [],
    }
    rid += 1;
  }
  res.json(resBody);
});

router.post('/requests/import', (req, res) => {
  res
    // .status(500)
    .json({
      "requestId": "aaa0753c-4feb-4b3a-9d31-8dd96e8b3e78",
      "started": true
    });
});

router.post('/geo/route', (req, res) => {
  res.json(calcRoute);
});

router.post('/tariffs/calculate/cargoCalculate', (req, res) => {
  res
    .json(tariffsCalculateAll);
});

router.post('/tariffs/calculate/enum/DEDICATED', (req, res) => {
  res
    .status(500)
    .json(tariffsCalculateDedicate);
});

router.post('/tariffs/calculate/enum/COURIER', (req, res) => {
  res.json(tariffsCalculateCourier);
});

router.post('/tariffs/calculate/enum/INTERREGIONAL', (req, res) => {
  res.json(tariffsCalculateInterregional);
});

router.get('/geo/address', (req, res) => {
  res.json(getAddressList);
});

router.get('/organizations/cargo/type/search', (req, res) => {
  // const data = /ноу/.test(req.query.text) ? getCargoTypes : [];
  // res.json(data);
  res.json(getCargoTypes);
});

router.post('/osago/predict', (req, res) => {
  res.json(personalCarOsago);
});

router.post('/requests/cargo/self/non_terminal', (req, res) => {
  res.json(nonTerminal);
});

router.post('/requests/cargo/self/terminal', (req, res) => {
  res.json(terminal);
});

router.get('/requests/cargo/:id', (req, res) => {
  res.json(getCargoRequest);
});

router.get('/requests/cargo/:id/status/history', (req, res) => {
  res.json(getCargoHistory);
});

router.post('/requests/cargo/template/self/non_terminal', (req, res) => {
  res.json(nonTerminalRegular);
});

router.post('/requests/cargo/template/self/terminal', (req, res) => {
  res.json(terminalRegular);
});

router.get('/requests/cargo/template/:id', (req, res) => {
  res.json(getRegularCargoRequest);
});

router.get('/requests/cargo/template/:id/status/history', (req, res) => {
  res.json(getCargoHistory);
});

router.post('/requests/evaluation/', (req, res) => {
  res.json(postEvaluation);
});

router.get('/requests/evaluation/:id', (req, res) => {
  res.json(getEvaluation);
});

module.exports = router;
