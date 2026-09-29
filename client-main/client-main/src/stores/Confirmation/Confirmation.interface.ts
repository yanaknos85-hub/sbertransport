export interface PhoneConfirmationRequest {
  code: string;
}

export interface BlockPhoneConfirmation {
  blocked: boolean;
  nextAllowTime?: string;
}
