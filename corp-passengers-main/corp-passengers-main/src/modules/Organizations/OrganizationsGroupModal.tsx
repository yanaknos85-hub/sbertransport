import React, { FC, useEffect } from 'react';

import { Modal } from 'shared/components/Modal/Modal';
import Input from 'shared/form/Input/Input';
import Select from 'shared/form/Select/Select';
import { Option } from 'shared/components/Select';
import { Organization } from 'stores/Organizations/Organizations.interface';
import { useOrganizationProjection } from 'api/organizations/search';
import { useCreateOrganizationGroup, useEditOrganizationsGroup } from 'api/organizations/organizations-groups';

import { useForm } from 'antd/lib/form/Form';
import { OrganizationsGroup } from 'stores/OrganizationsGroup/OrganizationsGroup.interface';
import { Form } from 'antd';
import { FormItem } from 'shared/components/FormItem';
import style from './Organizations/style.module.scss';
import { ignore } from 'utils';

interface OrganizationsGroupModalProps {
  visible: boolean;
  onCancel: () => void;
  group?: OrganizationsGroup;
}

const OrganizationsGroupModal: FC<OrganizationsGroupModalProps> = ({
  visible, onCancel, group,
}) => {
  const organizations: Organization[] = useOrganizationProjection({}, { suspense: false, enabled: visible }).data;

  // Костыль, т.к. в модель данных группы на гет не добавили список организаций, входящих в эту группу
  // eslint-disable-next-line @stylistic/max-len
  const groupOrgs = useOrganizationProjection({ query: { groupId: group?.id } }, { suspense: false, enabled: !!group && visible })
    ?.data
    ?.map(({ id }) => id) ?? [];

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const organizationIds = group ? groupOrgs : [];

  const [form] = useForm();

  useEffect(() => {
    form.resetFields();
  }, [group, organizationIds, form]);

  const [createOrgGroup, { isLoading: isLoadiongCreate }] = useCreateOrganizationGroup();
  const [editOrgGroup, { isLoading: isLoadingEdit }] = useEditOrganizationsGroup();

  const onOk = ({ name, organizationIds }: Omit<OrganizationsGroup, 'id'>) => {
    if (group) {
      editOrgGroup({
        id: group.id,
        name,
        organizationIds,
      })
        .then(onCancel)
        .catch(ignore);
    } else {
      createOrgGroup({
        name,
        organizationIds,
      })
        .then(onCancel)
        .catch(ignore);
    }
  };

  return (
    <Modal
      visible={visible}
      className={style.modal}
      onCancel={onCancel}
      onOk={form.submit}
      okButtonProps={{ loading: isLoadiongCreate || isLoadingEdit }}
    >
      <Form
        form={form}
        initialValues={{ ...group, organizationIds }}
        onFinish={onOk}
      >
        <FormItem name="name" label="Наименование группы">
          <Input />
        </FormItem>

        <FormItem name="organizationIds" label="Выбрать организации группы">
          <Select mode="multiple">
            {organizations?.map(org => (
              <Option value={org.id} key={org.id}>
                {org.officialName}
              </Option>
            ))}
          </Select>
        </FormItem>
      </Form>
    </Modal>
  );
};

export default OrganizationsGroupModal;
