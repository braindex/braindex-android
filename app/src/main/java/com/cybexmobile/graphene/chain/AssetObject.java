package com.cybexmobile.graphene.chain;


import java.io.Serializable;

public class AssetObject implements Serializable {

    public class asset_object_legible {
        public String count;
        public String decimal;
        public String symbol;

        public long lCount;
        public long lDecimal;
        public long scaled_precision;
    }

    //static const uint8_t space_id = protocol_ids;
    //static const uint8_t type_id  = asset_object_type;
    /// Ticker symbol for this Asset, i.e. "USD"
    public ObjectId<AssetObject> id;
    public String symbol;
    /// Maximum number of digits after the decimal point (must be <= 12)
    public int precision = 0;
    /// ID of the account which issued this Asset.
    ObjectId<AccountObject> issuer;

    public AssetOptions options;

    /// Current supply, fee pool, and collected fees are stored in a separate object as they change frequently.
    ObjectId<AssetDynamicDataObject> dynamic_asset_data_id;

    public boolean is_base_asset_object() {
        Asset base = options.core_exchange_rate.base;
        Asset quote = options.core_exchange_rate.quote;

        return base.asset_id.equals(quote.asset_id);
    }

    public asset_object_legible get_legible_asset_object(long amount) {
        long scaled_precision = get_scaled_precision();

        asset_object_legible assetObjectLegible = new asset_object_legible();
        assetObjectLegible.count = ((Long)(amount / scaled_precision)).toString();
        assetObjectLegible.decimal = ((Long)(amount % scaled_precision + scaled_precision)).toString().substring(1);
        assetObjectLegible.symbol = symbol;

        assetObjectLegible.lDecimal = amount % scaled_precision;
        assetObjectLegible.scaled_precision = scaled_precision;
        assetObjectLegible.lCount = amount / scaled_precision;

        return assetObjectLegible;
    }

    public long convert_exchange_to_base(long amount) {
        Asset base = options.core_exchange_rate.base;
        Asset quote = options.core_exchange_rate.quote;
        if (base.asset_id.equals(quote.asset_id)) {
            return amount;
        }

        long lBaseAmount = (long)(amount * ((double)quote.amount / base.amount));
        return lBaseAmount;
    }

    public long convert_exchange_from_base(long amount) {
        Asset base = options.core_exchange_rate.base;
        Asset quote = options.core_exchange_rate.quote;
        if (base.asset_id.equals(quote.asset_id)) {
            return amount;
        }

        long lQuoteAmount = (long)(amount * ((double)base.amount / quote.amount));
        return lQuoteAmount;
    }

    public Asset amount_from_string(String strAmount) {
        //strAmount.matches();
        // // TODO: 07/09/2017 需要正则表达处理


        long lCount = 0;
        long lDecimal = 0;
        Long scaled_precision = get_scaled_precision();

        int nIndex = strAmount.indexOf('.');
        if (nIndex == -1) {
            lCount = Long.valueOf(strAmount);
        } else {
            lCount = Long.valueOf(strAmount.substring(0, nIndex));

            int nDecMaxLen = scaled_precision.toString().substring(1).length();

            String strDecimal = strAmount.substring(nIndex + 1);
            for (int i = strDecimal.length(); i < nDecMaxLen; ++i) {
                strDecimal += "0";
            }

            lDecimal = Long.valueOf(strDecimal);
        }

        Asset assetObject = new Asset(lCount * scaled_precision + lDecimal, id);

        return assetObject;
    }

    public long get_scaled_precision() {
        long scaled_precision = 1;
        for (int i = 0; i < precision; ++i) {
            scaled_precision *= 10;
        }
        return scaled_precision;
    }
}
