// Simple Stripe backend for PaymentIntent creation
// Chạy: ../run_stripe_backend.sh hoặc: cd stripe_backend && npm install && node server.js

require('dotenv').config();
const express = require('express');
const Stripe = require('stripe');
const cors = require('cors');

// Load Stripe secret key from environment variable
// Create a .env file with: STRIPE_SECRET_KEY=your_stripe_secret_key_here
const stripeSecretKey = process.env.STRIPE_SECRET_KEY || 'sk_test_your_key_here';
const stripe = Stripe(stripeSecretKey);

const app = express();
const PORT = 4242;

app.use(cors());
app.use(express.json());

app.get('/', (_req, res) => {
  res.json({ status: 'ok', message: 'Stripe backend is running' });
});

app.post('/create-payment-intent', async (req, res) => {
  try {
    const { amount, currency, billCode, description } = req.body;

    if (!amount || !currency) {
      return res.status(400).json({ error: 'Missing amount or currency' });
    }

    // Ưu tiên description từ request, sau đó billCode, cuối cùng là mặc định
    let paymentDescription = description;
    if (!paymentDescription && billCode) {
      paymentDescription = `Thanh toán hóa đơn ${billCode}`;
    }
    if (!paymentDescription) {
      paymentDescription = 'TDTU Mobile Banking payment';
    }

    const paymentIntent = await stripe.paymentIntents.create({
      amount, // ví dụ: 500000 (VND)
      currency, // "vnd"
      description: paymentDescription,
      automatic_payment_methods: {
        enabled: true,
      },
    });

    res.json({ clientSecret: paymentIntent.client_secret });
  } catch (err) {
    console.error('Stripe error:', err);
    res.status(500).json({ error: err.message });
  }
});

app.listen(PORT, () => {
  console.log(`Stripe backend đang chạy tại http://localhost:${PORT}`);
  console.log('Emulator Android truy cập ở http://10.0.2.2:4242');
});


