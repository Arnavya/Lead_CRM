package com.sst.mini_lead_crm.enums;
/**
 * Represents the different stages of a lead
 * in the CRM lifecycle.
 */
public enum LeadStatus {

    /** Newly created lead. */
    NEW,

    /** Lead has been contacted. */
    CONTACTED,

    /** Lead is considered a potential customer. */
    QUALIFIED,

    /** Lead has been successfully converted into a customer. */
    CONVERTED,

    /** Lead is no longer interested or could not be converted. */
    LOST;

    /**
     * Checks whether the current status
     * can transition to the given next status.
     *
     * @param nextStatus the target status
     * @return true if transition is allowed, otherwise false
     */
    public boolean canTransitionTo(LeadStatus nextStatus) {

        return switch (this) {

            case NEW ->
                    nextStatus == CONTACTED ||
                            nextStatus == LOST;

            case CONTACTED ->
                    nextStatus == QUALIFIED ||
                            nextStatus == LOST;

            case QUALIFIED ->
                    nextStatus == CONVERTED ||
                            nextStatus == LOST;

            case CONVERTED, LOST -> false;
        };
    }
}