import { DeleteOutlined, RollbackOutlined, SaveOutlined } from '@ant-design/icons';
import { debounce } from '@material-ui/core';
import {
  Button, Form, Input, Popconfirm, Select, Tree
} from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { LabeledValue } from 'antd/lib/select';
import {
  useCreateDepartment, useDeleteDepartment, useDepartments, useUpdateDepartment
} from 'api/departments';
import { useSearchDepartment } from 'api/departments/search';
import { useSearchEmployee } from 'api/employee/search';
import { useOrganizations } from 'api/organizations';
import { useProfile } from 'api/profile';
import { useGetListRegions } from 'api/tariffs';
import { useTranslation } from 'i18n';
import { SelectDepartment } from 'shared/components/SelectDepartment';
import React, {
  Fragment, useCallback, useEffect, useState
} from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Organization, ShortDepartment } from 'stores/Corporate/Corporate.interface';
import { Department } from 'stores/Department/Department.interface';
import styled from 'styled-components';
import { useDebounce } from 'use-debounce';
import { preventDefault } from 'utils';
import { formatName } from 'utils/formatName';
import { fullName } from 'utils/employee';
import { UUID } from 'utils/io-ts';
import { searchSymbol } from 'utils/searchSymbol';
import { createTree } from 'utils/treeUtils';

const StyledForm = styled(Form)`
  margin-top: 32px;
`;

const FormBody = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  & .ant-form-item {
    display: contents;
  }
`;

const SelectOrganization: React.FC<React.ComponentProps<typeof Select>> = props => {
  const { content: organizations } = useOrganizations().data.organizationResponse;
  const { organizationId } = useProfile().data;
  return (
    <Select
      {...props}
      options={organizations.map(({ id, officialName }: Organization) => ({ value: id, label: officialName }))}
      defaultValue={organizationId}
    />
  );
};

const SelectStatus: React.FC<React.ComponentProps<typeof Select>> = props => (
  <Select
    {...props}
    options={[
      { value: 'ACTIVE', label: 'Активно' },
      { value: 'INACTIVE', label: 'Неактивно' },
    ]}
  />
);

const FullPath = ({ fullStructurePath }: { fullStructurePath: string }) => {
  const data = fullStructurePath.split('/') || [];
  const expandedKeys = Array.from({ length: data.length }).map((_, i) => i);
  const tree = createTree(data);

  return <Tree expandedKeys={expandedKeys} treeData={tree} />;
};

const Children = ({ childDepartments }: { childDepartments: ShortDepartment[] }) => (
  <span>{childDepartments.map(({ departmentName }) => departmentName).join(', ')}</span>
);

const FormButtons = styled.div`
  margin: 32px auto;
  display: flex;
  flex-direction: row;
  justify-content: center;

  button + button {
    margin-left: 16px;
  }
`;

const { onlyDigits, maxLength } = ValidationRules.general;

const DepartmentDetailed: React.FC = () => {
  const { t } = useTranslation();
  const { organizationId } = useProfile().data;
  const { byId: organizationsById } = useOrganizations().data;
  // @ts-ignore
  const { byId: departmentsById } = useDepartments(organizationId).data;
  const { logger } = useAppStoreContext();
  const geoZonesList = useGetListRegions().data;
  const geoZonesOptions: LabeledValue[] = geoZonesList.map(({ name, id }) => ({ label: name, value: id }));
  const minStringLength = 3;
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(Infinity);
  const [personalNumber, setPersonalNumber] = useState<string | undefined>();

  const history = useHistory();
  const match = useRouteMatch<{ id: UUID }>();
  const humanReadableId = match.params.id;
  const adding = !humanReadableId || humanReadableId === 'adding';

  const initiaDepartmentlValues = useSearchDepartment({
    query: { humanReadableId },
    // @ts-ignore
    orgId: organizationId,
  })?.data?.content[0];

  const departmentId = initiaDepartmentlValues?.id;
  const { departmentHead } = initiaDepartmentlValues || {};

  const initialValues = {
    ...(initiaDepartmentlValues ?? {
      children: [],
      employees: [],
      fullStructurePath: '',
      organizationId,
      departmentHeadId: departmentHead?.id,
    }),
    parentId: initiaDepartmentlValues?.parent?.id,
  };

  const departmentsLength = useSearchDepartment({
    pagination: { page: 0, size: 1 },
    projection: 'FULL',
    // @ts-ignore
    orgId: organizationId,
  }).data.content.length;

  const isHeadDepartment = adding ? !departmentsLength : !initiaDepartmentlValues.parent;

  const [createDepartment] = useCreateDepartment();
  const [updateDepartment] = useUpdateDepartment();
  // @ts-ignore
  const [deleteDepartment] = useDeleteDepartment(organizationId);
  const [departmentForm] = useForm();

  const { data, isLoading } = useSearchEmployee(
    { personnelNumber: personalNumber },
    { page: page ?? 0, size: 20 },
    // @ts-ignore
    organizationId,
    {
      cacheTime: 0,
      staleTime: 0,
      suspense: false,
      refetchOnWindowFocus: false,
      retry: true,
    }
  );

  const searchLocation = (value: string): void => {
    if (value.length > minStringLength || value === '') {
      setPersonalNumber(value);
    }
  };

  const [editSingleAddress] = useDebounce(searchLocation, 1000);

  const handleScroll = useCallback(
    e => {
      const target = e.target as HTMLDivElement;
      if (isLoading || page >= totalPages) {
        return;
      }

      if (target.scrollTop + target.offsetHeight + 200 < target.scrollHeight) {
        return;
      }

      setPage(p => p + 1);
    },
    [page, totalPages, isLoading]
  );

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleSave = async (values: any) => {
    const department: Department = {
      ...initialValues,
      ...values,
      fullStructurePath: `${values.parentId
        ? departmentsById[values.parentId]?.fullStructurePath
        : organizationsById[organizationId]?.officialName
      }/${values.departmentName}`,
      location: geoZonesList.find(c => c.id === values.geozoneId)?.name,
      parent: { id: values.parentId },
      departmentHead: { id: values.departmentHeadId },
    };

    if (!adding) {
      // @ts-ignore
      await updateDepartment({ orgId: organizationId, department });

      return;
    }

    await createDepartment(department)
      .then(newDepartment => {
        logger.toMessage('info', 'Запись добавлена');
        history.push(newDepartment?.humanReadableId ?? '');
      })
      .catch(error => {
        if (error.response?.data?.message.includes(t.Departments.DepartmentAddDuplicateError)) {
          logger.toMessage(
            'error',
            `Подразделение с кодом
              ${error.response.data.problems && error.response.data.problems[0].value} уже существует`
          );
        } else if (error.response.data.message.includes(t.Departments.DepartmentAddParentError)) {
          logger.toMessage('error', t.Departments.DepartmentAddParentError);
        }
      });
  };

  // eslint-disable-next-line consistent-return
  const handleDelete = async () => {
    if (!adding) {
      // @ts-ignore
      return deleteDepartment({ orgId: organizationId, depId: departmentId as UUID });
    }
  };

  const goBack = useCallback(() => history.push('./'), [history]);

  // Ниже будет все то, что связано с поиском сотрудников. При переносе в отдельный компонент form теряет компонент :(
  // В конце концов изменение состояния form не происходило. Хотя нативные ...props передавались, antd - черный ящик.

  const [allEmployees, setAllEmployees] = useState(data?.content ?? []);

  useEffect(() => {
    editSingleAddress(personalNumber ?? '');
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, personalNumber]);

  useEffect(() => {
    data?.content && setAllEmployees(oldEmployees => [...oldEmployees, ...data.content]);
  }, [data?.content, isLoading]);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const searchHandler = useCallback(
    debounce((number: string) => {
      setTotalPages(Infinity);
      setAllEmployees([]);
      setPage(0);
      editSingleAddress(number);
    }, 500),
    []
  );

  const handleDelegates = (e: React.MouseEvent<HTMLElement>) => {
    e.preventDefault();
    history.push(
      `/directories/departments/delegates?superviserId=${departmentHead?.id}&departmentId=${initialValues.id}`
    );
  };

  return (
    <StyledForm
      initialValues={initialValues}
      onFinish={handleSave}
      key={departmentId}
      form={departmentForm}
    >
      <FormBody>
        {adding ? null : (
          <>
            <Form.Item label="ID подразделения" name="humanReadableId">
              <Input disabled />
            </Form.Item>

            <Form.Item label="Признак активности" name="status">
              <SelectStatus disabled />
            </Form.Item>
          </>
        )}

        <Form.Item label="Корп. клиент" name="organizationId">
          {/* @ts-ignore  */}
          <SelectOrganization disabled={organizationId && true} />
        </Form.Item>

        <Form.Item
          label="Наименование подразделения"
          name="departmentName"
          rules={[{ required: true }, maxLength(255)]}
        >
          <Input maxLength={255} onPressEnter={preventDefault} />
        </Form.Item>

        <Form.Item
          label="Код подразделения"
          name="code"
          rules={[{ required: true }, onlyDigits, maxLength(10)]}
        >
          <Input maxLength={10} onPressEnter={preventDefault} />
        </Form.Item>

        {!isHeadDepartment
        && (
        <Form.Item
          label="Родительское подразделение"
          name="parentId"
          rules={[{ required: true }]}
        >
          <SelectDepartment
            allowClear
            onInputKeyDown={preventDefault}
            showId
            label={initiaDepartmentlValues?.parent?.departmentName}
          />
        </Form.Item>
        )}

        <Form.Item
          label={(
            <div>
              {departmentHead?.id && (
                <Button onClick={handleDelegates} style={{ marginRight: 10 }}>
                  Делегаты
                </Button>
              )}
              <span>Руководитель</span>
            </div>
          )}
          name="departmentHeadId"
        >
          <Select
            allowClear
            autoClearSearchValue
            onPopupScroll={handleScroll}
            onSearch={searchHandler}
            showSearch
            filterOption={false}
            loading={isLoading}
            options={allEmployees.map(({
              id, lastName, firstName, patronymic, personnelNumber,
            }) => ({
              value: id,
              label: `${fullName({
                firstName, lastName, patronymic,
              })} - ${personnelNumber}`,
            }))}
            placeholder={`${fullName({
              firstName: departmentHead?.firstName,
              lastName: departmentHead?.lastName,
              patronymic: departmentHead?.patronymic,
            })}`}
          />
        </Form.Item>

        <Form.Item label="Территориальное местоположение" name="geozoneId">
          <Select
            options={geoZonesOptions}
            showSearch
            optionFilterProp="label"
            filterOption={searchSymbol}
            onInputKeyDown={preventDefault}
          />
        </Form.Item>

        {!adding && (
          <>
            <Form.Item label="Путь подразделения">
              <FullPath fullStructurePath={initialValues.fullStructurePath ?? ''} />
            </Form.Item>
            <Form.Item label="Дочерние подразделения">
              <Children childDepartments={initialValues.children} />
            </Form.Item>
            <Form.Item label="Сотрудники">
              {initialValues.employees.map(item => (
                <Fragment key={item.id}>{item ? `${formatName(item)} (${item.personnelNumber}), ` : ''}</Fragment>
              ))}
            </Form.Item>
          </>
        )}

        <Form.Item label="ID ЕАСУП" name="easupId">
          <Input disabled />
        </Form.Item>
      </FormBody>

      <FormButtons>
        <Button
          icon={<SaveOutlined />}
          size="middle"
          htmlType="submit"
        >
          Сохранить
        </Button>
        {adding ? null : (
          <Popconfirm
            placement="top"
            title="Удалить подразделение?"
            onConfirm={handleDelete}
            okText="OK"
            cancelText="Отменить"
            style={{ width: 300 }}
          >
            <Button
              icon={<DeleteOutlined />}
              size="middle"
              danger
            >
              Удалить
            </Button>
          </Popconfirm>
        )}
        <Popconfirm
          placement="top"
          title="Отменить?"
          onConfirm={goBack}
          okText="OK"
          cancelText="Не отменять"
          style={{ width: 300 }}
        >
          <Button icon={<RollbackOutlined />} size="middle">
            Отменить
          </Button>
        </Popconfirm>
      </FormButtons>
    </StyledForm>
  );
};

export default DepartmentDetailed;
