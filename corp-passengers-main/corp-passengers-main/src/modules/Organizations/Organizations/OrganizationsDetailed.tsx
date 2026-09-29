import React, { useCallback, useMemo } from 'react';
import { useRouteMatch } from 'react-router-dom';

import {
  useCreateOrganization,
  useGetOrganizationById,
  useUpdateOrganization
} from 'api/organizations';
import { Organization } from 'stores/Organizations/Organizations.interface';
import { UUID } from 'utils/io-ts';
import { ModelDetailPage } from 'shared/models/ModelDetail/ModelDetailPage';
import { useTranslation } from 'i18n';
import { useFields } from './Fields';

type OrganizationForm = Omit<Organization, 'contacts'> & { email: string; site: string; phone: string };

const OrganizationsDetailed: React.FC<JSX.Element> = (): JSX.Element => {
  const { t } = useTranslation();
  const { id } = useRouteMatch<{ id: UUID }>().params;
  const isAdding = id === 'adding';
  const [createOrganization] = useCreateOrganization();
  const [updateOrganization] = useUpdateOrganization();

  const organization = useGetOrganizationById(id, { enabled: !isAdding }).data;

  const fields = useFields(isAdding, isAdding || organization?.status === 'ACTIVE');

  const contacts = useMemo(() => {
    if (!organization) return {};

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return organization?.contacts?.reduce((res: any, contact: any) => {
      const propertyName = contact.type.toLowerCase();
      res[propertyName] = contact.value;
      return res;
    }, Object.create(null));
  },
  [organization]
  );

  const initialValues = useMemo(
    () => ({
      ...(organization || {}),
      ...contacts,
    }),
    [organization, contacts]
  );

  const handleSave = useCallback(async ({
    email, site, phone, ...values
  }: OrganizationForm) => {
    const contacts: Organization['contacts'] = [];

    if (phone) {
      contacts.push({ type: 'PHONE', value: `+${phone.replace(/[^\d]/g, '')}` });
    }

    if (email) {
      contacts.push({ type: 'EMAIL', value: email });
    }

    if (site) {
      contacts.push({ type: 'SITE', value: site });
    }

    const newOrganization: Organization = {
      ...values,
      contacts,
    };

    return (isAdding ? createOrganization : updateOrganization)(newOrganization);
  },
  [createOrganization, updateOrganization, isAdding]
  );

  return (
    <ModelDetailPage
      fields={fields}
      initialValues={initialValues}
      handleSave={handleSave}
      isNew={isAdding}
      pageHeader={t.contractors.backToContractors}
    />
  );
};

export default OrganizationsDetailed;
