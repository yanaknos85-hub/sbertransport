import * as t from 'io-ts';

import * as tt from 'utils/io-ts';
// Список статусов взят из DTO: Заявка (чтение);
const approvalStateStatuses = ['AWAITING_APPROVAL', 'APPROVED', 'DECLINED'] as const;

export const ApprovalStateStatuses = tt.oneOf([...approvalStateStatuses]);
export type ApprovalStateStatuses = t.TypeOf<typeof ApprovalStateStatuses>;
